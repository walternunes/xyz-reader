package com.example.xyzreader.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.ShareCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.graphics.Palette;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
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

    private long mSelectedItemId;
    private int mSelectedItemUpButtonFloor = Integer.MAX_VALUE;
    private int mTopInset;

    private ViewPager mPager;
    private MyPagerAdapter mPagerAdapter;
    private int mCurrentItem;
  //  private View mUpButtonContainer;
  //  private View mUpButton;


    //V2 change
    private int mMutedColor = 0xFF333333;
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
        setContentView(R.layout.activity_article_detail_v2);

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
            /*    mUpButton.animate()
                        .alpha((state == ViewPager.SCROLL_STATE_IDLE) ? 1f : 0f)
                        .setDuration(300);*/
            }

            @Override
            public void onPageSelected(int position) {
                System.out.println("fano >>");
                if (mCursor != null) {
                    mCursor.moveToPosition(position);
                }
                mSelectedItemId = mCursor.getLong(ArticleLoader.Query._ID);
                loadImageCollapsed(mCursor.getString(ArticleLoader.Query.PHOTO_URL));
                loadTitle(mCursor.getString(ArticleLoader.Query.TITLE));
              //  updateUpButtonPosition();
            }
        });
/*
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

       /* if(getIntent().hasExtra(ArticleListActivity.EXTRA_TITLE_STRING))
            loadTitle(getIntent().getExtras().getString(ArticleListActivity.EXTRA_TITLE_STRING));
        getIntent().removeExtra(ArticleListActivity.EXTRA_TITLE_STRING);*/
        mToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSupportNavigateUp();
            }
        });

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


    @Override
    protected void onResume() {
        super.onResume();
        if(mCursor != null) {
            loadImageCollapsed(mCursor.getString(ArticleLoader.Query.PHOTO_URL));
            loadTitle(mCursor.getString(ArticleLoader.Query.TITLE));
        }
    }

    private void loadTitle(String title){
        System.out.println("fano load");
        mCollapsingToolbar.setTitle(title);
        mCollapsingToolbar.setExpandedTitleTextAppearance(R.style.TextAppearance_AppCompat_Medium);
    }
    private void loadImageCollapsed(String url){
        System.out.println("fano>>" + url);
        ImageLoaderHelper.getInstance(this).getImageLoader()
                .get(url, new ImageLoader.ImageListener() {
                    @Override
                    public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                        Bitmap bitmap = imageContainer.getBitmap();
                        mPhotoView.setImageBitmap(bitmap);
                        System.out.println("fano color");

                        if (bitmap != null) {
                            System.out.println("fano color2");
                            Palette p = Palette.from(bitmap).generate();

                            mMutedColor = p.getDarkVibrantColor(getResources().getColor(R.color.theme_primary_dark));
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                Window window = getWindow();

                                window.setStatusBarColor(mMutedColor);
                            }

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
        System.out.println("banco teste");
        // Select the start ID
        if (mStartId > 0) {
            mCursor.moveToFirst();
            // TODO: optimize
            while (!mCursor.isAfterLast()) {
                if (mCursor.getLong(ArticleLoader.Query._ID) == mStartId) {
                    System.out.println("banco teste2");
                    loadTitle(mCursor.getString(ArticleLoader.Query.TITLE));
                    loadImageCollapsed(mCursor.getString(ArticleLoader.Query.PHOTO_URL));
                    final int position = mCursor.getPosition();
                    mPager.setCurrentItem(position, false);
                    break;
                }
                mCursor.moveToNext();
            }
            mStartId = 0;
        }else mPager.setCurrentItem(0, false);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        mPagerAdapter.notifyDataSetChanged();
    }
/*
    public void onUpButtonFloorChanged(long itemId, ArticleDetailFragment fragment) {
        if (itemId == mSelectedItemId) {
            mSelectedItemUpButtonFloor = fragment.getUpButtonFloor();
            updateUpButtonPosition();
        }
    }
*/
   /* private void updateUpButtonPosition() {
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
            BlankFragment fragment = (BlankFragment) object;
            if (fragment != null) {
              //  mSelectedItemUpButtonFloor = fragment.getUpButtonFloor();
               // updateUpButtonPosition();
            }
        }

        @Override
        public Fragment getItem(int position) {
            mCursor.moveToPosition(position);
            return BlankFragment.newInstance(mCursor.getLong(ArticleLoader.Query._ID));
        }

        @Override
        public int getCount() {
            return (mCursor != null) ? mCursor.getCount() : 0;
        }
    }
}
