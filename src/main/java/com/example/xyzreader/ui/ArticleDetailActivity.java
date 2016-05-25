package com.example.xyzreader.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.ShareCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.graphics.Palette;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity extends ActionBarActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private Cursor mCursor;
    private long mStartId;

    // V2 - warning - will be changed later
    private long mSelectedItemId;
    private int mSelectedItemUpButtonFloor = Integer.MAX_VALUE;
    private int mTopInset;

    private ViewPager mPager;
    private MyPagerAdapter mPagerAdapter;
    private int mCurrentItem;
  // V2 remove
  //  private View mUpButtonContainer;
  //  private View mUpButton;


    //V2 change
    private int mMutedColorDark;
    private int mMutedColorPrimary;
    private ImageView mPhotoView;
    private FloatingActionButton mShareFab;
    private android.support.v7.widget.Toolbar mToolbar;
    private android.support.design.widget.CollapsingToolbarLayout mCollapsingToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }

        // V2 change
        setContentView(R.layout.activity_article_detail_v2);

        // V2 - general changes for new layout
        getLoaderManager().initLoader(0, null, this);
        mPhotoView = (ImageView) findViewById(R.id.collapse_photo);
        mToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        mCollapsingToolbar = (android.support.design.widget.CollapsingToolbarLayout)
                findViewById(R.id.detail_collapsing_toolbar);
        mPagerAdapter = new MyPagerAdapter(getFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);
        mPager.setPageMargin((int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        mPager.setPageMarginDrawable(new ColorDrawable(0x22000000));

        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                // V2 removed
            /*    mUpButton.animate()
                        .alpha((state == ViewPager.SCROLL_STATE_IDLE) ? 1f : 0f)
                        .setDuration(300);*/
            }

            @Override
            public void onPageSelected(int position) {
                if (mCursor != null) {
                    mCursor.moveToPosition(position);
                }
                mSelectedItemId = mCursor.getLong(ArticleLoader.Query._ID);
                // V2 add
                loadImageCollapsed(mCursor.getString(ArticleLoader.Query.PHOTO_URL));
                loadTitle(mCursor.getString(ArticleLoader.Query.TITLE));

                // V2 change - warning will be fixed later
              //  updateUpButtonPosition();
            }
        });
/*     V2 Remove
        mUpButtonContainer = findViewById(R.id.up_container);

        mUpButton = findViewById(R.id.action_up);
        mUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSupportNavigateUp();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mUpButtonContainer.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                @Override
                public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                    view.onApplyWindowInsets(windowInsets);
                    mTopInset = windowInsets.getSystemWindowInsetTop();
                    mUpButtonContainer.setTranslationY(mTopInset);
                    updateUpButtonPosition();
                    return windowInsets;
                }
            });
        }*/

        // V2 add
        mToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSupportNavigateUp();
            }
        });

        // V2 add
        final Activity activity = this;
        mShareFab = (FloatingActionButton) findViewById(R.id.share_fab);
        mShareFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(activity)
                        // TODO link share intent to displayed pager
                        .setType("text/plain")
                        .setText("Some sample text")
                        .getIntent(), getString(R.string.action_share)));
            }
        });

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                mStartId = ItemsContract.Items.getItemId(getIntent().getData());
                mSelectedItemId = mStartId;
            }
        }


    }

    // V2 add
    @Override
    protected void onResume() {
        super.onResume();
        if(mCursor != null) {
            loadImageCollapsed(mCursor.getString(ArticleLoader.Query.PHOTO_URL));
            loadTitle(mCursor.getString(ArticleLoader.Query.TITLE));
        }
    }

    /** V3 - to remove animation
    @Override
    public void onBackPressed() {
        finish();
    }
*/
    // V2 add
    private void loadTitle(String title){
        mCollapsingToolbar.setTitle(title);
        mCollapsingToolbar.setExpandedTitleTextAppearance(R.style.TextAppearance_AppCompat_Medium);
    }

    // V2 add
    private void loadImageCollapsed(String url){
        ImageLoaderHelper.getInstance(this).getImageLoader()
                .get(url, new ImageLoader.ImageListener() {
                    @Override
                    public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                        Bitmap bitmap = imageContainer.getBitmap();
                        mPhotoView.setImageBitmap(bitmap);

                        if (bitmap != null) {
                            Palette p = Palette.from(bitmap).generate();
                            Palette.Swatch swatch = p.getMutedSwatch();
                            Palette.Swatch swatchDark = p.getDarkMutedSwatch();
                            if(swatch != null && swatchDark != null) {
                                mMutedColorDark = swatchDark.getRgb();
                                mMutedColorPrimary = swatch.getRgb();
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
                                    Window window = getWindow();
                                    window.setStatusBarColor(mMutedColorDark);
                                    mCollapsingToolbar.setContentScrim(new ColorDrawable(mMutedColorPrimary));
                                }
                            }
                            // V2 warning - Fixed later
                            //updateStatusBar();
                        }
                    }

                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }

                });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mCursor = cursor;
        mPagerAdapter.notifyDataSetChanged();

        // Select the start ID
        if (mStartId > 0) {
            mCursor.moveToFirst();
            // TODO: optimize
            while (!mCursor.isAfterLast()) {
                if (mCursor.getLong(ArticleLoader.Query._ID) == mStartId) {

                    // V2 add
                    loadTitle(mCursor.getString(ArticleLoader.Query.TITLE));
                    loadImageCollapsed(mCursor.getString(ArticleLoader.Query.THUMB_URL));

                    final int position = mCursor.getPosition();
                    mPager.setCurrentItem(position, false);
                    break;
                }
                mCursor.moveToNext();
            }
            mStartId = 0;
        }//else mPager.setCurrentItem(0, false);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        mPagerAdapter.notifyDataSetChanged();
    }
/* V2 Removed
    public void onUpButtonFloorChanged(long itemId, ArticleDetailFragment fragment) {
        if (itemId == mSelectedItemId) {
            mSelectedItemUpButtonFloor = fragment.getUpButtonFloor();
            updateUpButtonPosition();
        }
    }
*/
   /* V2 - removed
   private void updateUpButtonPosition() {
        int upButtonNormalBottom = mTopInset + mUpButton.getHeight();
        mUpButton.setTranslationY(Math.min(mSelectedItemUpButtonFloor - upButtonNormalBottom, 0));
    }*/

    private class MyPagerAdapter extends FragmentStatePagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            // V2 changed - will be fixed later changing to Article Detail Fragment
            ArticleDetailFragment fragment = (ArticleDetailFragment) object;
            if (fragment != null) {
                // V2 warning - will be fixed later
              //  mSelectedItemUpButtonFloor = fragment.getUpButtonFloor();
               // updateUpButtonPosition();
            }
        }

        @Override
        public Fragment getItem(int position) {
            mCursor.moveToPosition(position);
            Fragment fragment = ArticleDetailFragment.newInstance(mCursor.getLong(ArticleLoader.Query._ID));

            // V2 changed - will be fixed later changing to Article Detail Fragment
            return fragment;
        }

        @Override
        public int getCount() {
            return (mCursor != null) ? mCursor.getCount() : 0;
        }
    }
}
