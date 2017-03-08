package io.connection.bluetooth.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.connection.bluetooth.Api.ApiCall;
import io.connection.bluetooth.Api.ApiClient;
import io.connection.bluetooth.Domain.GameCategory;
import io.connection.bluetooth.Domain.GameLibrary;
import io.connection.bluetooth.Domain.GameProfile;
import io.connection.bluetooth.Domain.User;
import io.connection.bluetooth.R;
import io.connection.bluetooth.utils.Constants;
import io.connection.bluetooth.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by songline on 29/09/16.
 */
public class GameProfileAddActivity extends AppCompatActivity {
    List<GameProfile> gameProfileList = null;
    Context context;
    RecyclerView gameProfileLayout;
    GameGridAdapter gameGridAdapter;
    LinearLayoutManager linearLayoutManager;
    TextView textView;
    ApiCall apiCall;
    Map<String, String> installedApps = new HashMap<>();
    Map<String, String> versionName = new HashMap<>();
    private static final String TAG = "GameProfileAddActivity";

    User user = new User();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_profile_add_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        gameProfileList = new ArrayList<>();
        context = this;
        apiCall = ApiClient.getClient().create(ApiCall.class);
        textView = (TextView) findViewById(R.id.nogamefound);
        PackageManager packageManager = this.getPackageManager();
        List<ApplicationInfo> applicationInfoList = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo applicationInfo : applicationInfoList) {

            if ((applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                //it's a system app, not interested
            } else if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                //Discard this one
                //in this case, it should be a user-installed app
            } else if (packageManager.getLaunchIntentForPackage(applicationInfo.packageName) != null) {
                installedApps.put(applicationInfo.packageName, applicationInfo.loadLabel(packageManager).toString());

                try {
                    versionName.put(applicationInfo.packageName, getPackageManager().getPackageInfo(applicationInfo.packageName, 0).versionName);
                } catch (Exception e) {
                    versionName.put(applicationInfo.packageName, "");
                }

            }

        }

        LoadGamesForAdd loadGamesForAdd = new LoadGamesForAdd();
        loadGamesForAdd.execute();

    }

    class LoadGamesForAdd extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            if (Utils.isConnected(context)) {
                SharedPreferences preference = getSharedPreferences(Constants.LOGIN, MODE_PRIVATE);
                user.setId(preference.getString(Constants.LOGIN_KEY, ""));

                List<String> packageNameList = new ArrayList<String>();
                packageNameList.addAll(installedApps.keySet());

                Call<List<String>> result = apiCall.getGamesForAddToProfile(user.getId(), packageNameList);
                result.enqueue(new Callback<List<String>>() {
                    @Override
                    public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                        System.out.println("Response : " + response + "   " + response.code());

                        if (response.code() == 200) {
                            List<String> finalPackageNameList = response.body();
                            for (String packageName : finalPackageNameList) {


                                PackageManager pm = context.getPackageManager();
                                try {
                                    ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
                                    //    if ((ai.flags & ApplicationInfo.FLAG_IS_GAME) == ApplicationInfo.FLAG_IS_GAME) {

                                    GameProfile game = new GameProfile();
                                    GameLibrary lib = new GameLibrary();
                                    GameCategory category = new GameCategory();
                                    lib.setGameCategoryId(category);
                                    lib.setGameName(installedApps.get(packageName));
                                    String version = versionName.get(packageName);
                                    if (!version.isEmpty()) {
                                        lib.setGameVersion(version);
                                    }
                                    lib.setPackageName(packageName);
                                    game.setGameLibrary(lib);
                                    gameProfileList.add(game);
                                    //  }
                                } catch (Exception e) {

                                }

                            }
                            if (gameProfileList.size() > 0) {

                                linearLayoutManager = new LinearLayoutManager(context);
                                linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                                gameProfileLayout = (RecyclerView) findViewById(R.id.game_profile_add_gridview);
                                gameGridAdapter = new GameGridAdapter(gameProfileList, context, linearLayoutManager);
                                setgameProfileLayout(gameProfileLayout);
                                textView.setVisibility(View.GONE);
                            } else
                                textView.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<String>> call, Throwable t) {
                        t.printStackTrace();
                        Toast toast = Toast.makeText(context, Constants.ERROR_MESSAGE, Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                        toast.show();

                    }
                });
            } else {
                Toast.makeText(getBaseContext(), Constants.INTERNET_ERROR_MESSAGE, Toast.LENGTH_LONG).show();
            }

            return null;
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    public void setgameProfileLayout(RecyclerView gameProfileLayout) {
        gameProfileLayout.setHasFixedSize(true);
        //gridLayoutManager = (GridLayoutManager) gameProfileLayout.getLayoutManager();
        gameProfileLayout.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        // gameProfileLayout.setLayoutManager(gridLayoutManager);
        gameProfileLayout.setAdapter(gameGridAdapter);
        setUpItemTouchHelper();
        setUpAnimationDecoratorHelper();

    }


    /**
     * This is the standard support library way of implementing "swipe to delete" feature. You can do custom drawing in onChildDraw method
     * but whatever you draw will disappear once the swipe is over, and while the items are animating to their new position the recycler view
     * background will be visible. That is rarely an desired effect.
     */
    private void setUpItemTouchHelper() {

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            // we want to cache these and not allocate anything repeatedly in the onChildDraw method
            Drawable background;
            Drawable xMark;
            int xMarkMargin;
            boolean initiated;

            private void init() {
                background = new ColorDrawable(Color.BLACK);
                xMark = ContextCompat.getDrawable(GameProfileAddActivity.this, R.drawable.ic_clear_24dp);
                xMark.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                xMarkMargin = (int) GameProfileAddActivity.this.getResources().getDimension(R.dimen.tab_padding_bottom);
                initiated = true;
            }

            // not important, we don't want drag & drop
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int position = viewHolder.getAdapterPosition();
                GameGridAdapter testAdapter = (GameGridAdapter) gameProfileLayout.getAdapter();
                if (testAdapter.isUndoOn() && testAdapter.isPendingRemoval(position)) {
                    return 0;
                }
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                if (Utils.isConnected(context)) {
                    int swipedPosition = viewHolder.getAdapterPosition();
                    GameGridAdapter adapter = (GameGridAdapter) gameProfileLayout.getAdapter();
                    boolean undoOn = adapter.isUndoOn();
                    if (undoOn) {
                        adapter.pendingRemoval(swipedPosition);
                    } else {
                        adapter.remove(swipedPosition);
                    }
                } else {
                    Toast.makeText(getBaseContext(), Constants.INTERNET_ERROR_MESSAGE, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;

                // not sure why, but this method get's called for viewholder that are already swiped away
                if (viewHolder.getAdapterPosition() == -1) {
                    // not interested in those
                    return;
                }

                if (!initiated) {
                    init();
                }

                // draw red background
                background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                background.draw(c);

                // draw x mark
                int itemHeight = itemView.getBottom() - itemView.getTop();
                int intrinsicWidth = xMark.getIntrinsicWidth();
                int intrinsicHeight = xMark.getIntrinsicWidth();

                int xMarkLeft = itemView.getRight() - xMarkMargin - intrinsicWidth;
                int xMarkRight = itemView.getRight() - xMarkMargin;
                int xMarkTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
                int xMarkBottom = xMarkTop + intrinsicHeight;
                xMark.setBounds(xMarkLeft, xMarkTop, xMarkRight, xMarkBottom);

                xMark.draw(c);

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

        };
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        mItemTouchHelper.attachToRecyclerView(gameProfileLayout);
    }


    /**
     * We're gonna setup another ItemDecorator that will draw the red background in the empty space while the items are animating to thier new positions
     * after an item is removed.
     */
    private void setUpAnimationDecoratorHelper() {
        gameProfileLayout.addItemDecoration(new RecyclerView.ItemDecoration() {

            // we want to cache this and not allocate anything repeatedly in the onDraw method
            Drawable background;
            boolean initiated;

            private void init() {
                background = new ColorDrawable(Color.BLACK);
                initiated = true;
            }

            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {

                if (!initiated) {
                    init();
                }

                // only if animation is in progress
                if (parent.getItemAnimator().isRunning()) {

                    // some items might be animating down and some items might be animating up to close the gap left by the removed item
                    // this is not exclusive, both movement can be happening at the same time
                    // to reproduce this leave just enough items so the first one and the last one would be just a little off screen
                    // then remove one from the middle

                    // find first child with translationY > 0
                    // and last one with translationY < 0
                    // we're after a rect that is not covered in recycler-view views at this point in time
                    View lastViewComingDown = null;
                    View firstViewComingUp = null;

                    // this is fixed
                    int left = 0;
                    int right = parent.getWidth();

                    // this we need to find out
                    int top = 0;
                    int bottom = 0;

                    // find relevant translating views
                    int childCount = parent.getLayoutManager().getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        View child = parent.getLayoutManager().getChildAt(i);
                        if (child.getTranslationY() < 0) {
                            // view is coming down
                            lastViewComingDown = child;
                        } else if (child.getTranslationY() > 0) {
                            // view is coming up
                            if (firstViewComingUp == null) {
                                firstViewComingUp = child;
                            }
                        }
                    }

                    if (lastViewComingDown != null && firstViewComingUp != null) {
                        // views are coming down AND going up to fill the void
                        top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
                        bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();
                    } else if (lastViewComingDown != null) {
                        // views are going down to fill the void
                        top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
                        bottom = lastViewComingDown.getBottom();
                    } else if (firstViewComingUp != null) {
                        // views are coming up to fill the void
                        top = firstViewComingUp.getTop();
                        bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();
                    }

                    background.setBounds(left, top, right, bottom);
                    background.draw(c);

                }
                super.onDraw(c, parent, state);
            }

        });
    }


    public class GameGridAdapter extends RecyclerView.Adapter<GameGridAdapter.ViewHolder> {

        List<GameProfile> gameProfileList = null;
        List<GameProfile> itemsPendingGameProfileList = null;
        Context context = null;
        boolean undoOn = true;
        private static final int PENDING_REMOVAL_TIMEOUT = 3000; // 3sec

        private Handler handler = new Handler(); // hanlder for running delayed runnables
        HashMap<GameProfile, Runnable> pendingRunnables = new HashMap<>();
        PackageManager packageManager;
        LinearLayoutManager layoutManager;


        public GameGridAdapter(List<GameProfile> gameProfiles, Context context, LinearLayoutManager layoutManager) {

            gameProfileList = gameProfiles;
            this.context = context;
            itemsPendingGameProfileList = new ArrayList<>();
            packageManager = context.getPackageManager();
            this.layoutManager = layoutManager;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (holder.getItemViewType() == 0) {
                final GameProfile gameProfile = gameProfileList.get(position);
                if (itemsPendingGameProfileList.contains(gameProfile)) {
                    holder.itemView.setBackgroundColor(Color.BLACK);
                    holder.app_name.setVisibility(View.GONE);
                    holder.app_logo.setVisibility(View.GONE);
                    holder.undoButton.setVisibility(View.VISIBLE);
                    holder.undoButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Runnable pendingRemovalRunnable = pendingRunnables.get(gameProfile);
                            pendingRunnables.remove(gameProfile);
                            if (pendingRemovalRunnable != null)
                                handler.removeCallbacks(pendingRemovalRunnable);
                            itemsPendingGameProfileList.remove(gameProfile);
                            // this will rebind the row in "normal" state
                            notifyItemChanged(gameProfileList.indexOf(gameProfile));
                        }
                    });
                } else {
                    holder.itemView.setBackgroundColor(Color.WHITE);
                    holder.app_logo.setVisibility(View.VISIBLE);
                    holder.app_name.setVisibility(View.VISIBLE);
                    Drawable icon;
                    try {
                        icon = packageManager.getApplicationIcon(gameProfile.getGameLibrary().getPackageName().toString());
                    } catch (Exception e) {
                        icon = getResources().getDrawable(R.drawable.ic_user);
                    }
                    holder.app_logo.setImageDrawable(icon);
                    holder.app_name.setText(gameProfile.getGameLibrary().getGameName());
                    holder.itemView.setTag(gameProfile);
                    holder.undoButton.setVisibility(View.GONE);
                    holder.undoButton.setOnClickListener(null);
                }
            }

        }

        @Override
        public int getItemCount() {
            return gameProfileList.size();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(context).inflate(viewType == 0 ? R.layout.game_profile_add_single_card : R.layout.searching_devices, parent, false), context, viewType);
        }

        public boolean isUndoOn() {
            return undoOn;
        }

        public void pendingRemoval(int position) {
            final GameProfile item = gameProfileList.get(position);
            if (!itemsPendingGameProfileList.contains(item)) {
                itemsPendingGameProfileList.add(item);
                // this will redraw row in "undo" state
                notifyItemChanged(position);
                // let's create, store and post a runnable to remove the item
                Runnable pendingRemovalRunnable = new Runnable() {
                    @Override
                    public void run() {
                        remove(gameProfileList.indexOf(item));
                    }
                };
                handler.postDelayed(pendingRemovalRunnable, PENDING_REMOVAL_TIMEOUT);
                pendingRunnables.put(item, pendingRemovalRunnable);
            }
        }

        public void remove(int position) {

            GameProfile item = gameProfileList.get(position);
            if (itemsPendingGameProfileList.contains(item)) {
                itemsPendingGameProfileList.remove(item);
            }
            if (gameProfileList.contains(item)) {
                gameProfileList.remove(position);
                notifyItemRemoved(position);
                if (gameProfileList.isEmpty()) {
                    textView.setVisibility(View.VISIBLE);
                }
            }

            if (!gameProfileList.contains(item) && !itemsPendingGameProfileList.contains(item)) {
                item.setUserId(user);
                if (item.getGameLibrary().getGameId() == null || item.getGameLibrary().getGameId().isEmpty()) {
                    addGameProfileRequestToLibrary(item);
                } else {
                    addGameProfileByUser(item);
                }
            }
        }


        public void addGameProfileByUser(GameProfile gameProfile) {


            Call<Object> resulObjectCall = apiCall.addGameToProfileByUser(gameProfile);
            resulObjectCall.enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {


                    if (response.isSuccessful()) {
                        Toast toast = Toast.makeText(context, "GameProfile Added Successfully", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.TOP, 0, 0);
                        toast.show();

                    } else {

                        Toast toast = Toast.makeText(context, "GameProfile Not Adding", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.TOP, 0, 0);
                        toast.show();

                    }


                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    Toast.makeText(context, "GameProfile Not Adding", Toast.LENGTH_LONG).show();
                }
            });


        }


        public void addGameProfileRequestToLibrary(GameProfile gameProfile) {

            Call<Object> resulObjectCall = apiCall.addGameProfileRequestToLibrary(gameProfile);
            resulObjectCall.enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {


                    if (response.isSuccessful() && response.code() >= 200 && response.code() <= 300) {
                        Toast toast = Toast.makeText(context, "Request Sent for Update GameLibrary ", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.TOP, 0, 0);
                        toast.show();

                    } else {

                        Log.d(TAG, "onResponse: " + response.message());
                        Toast toast = Toast.makeText(context, "Request Failed for Update GameLibrary", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.TOP, 0, 0);
                        toast.show();

                    }


                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    Toast.makeText(context, "Request Not sent for Update GameLibrary", Toast.LENGTH_LONG).show();
                }
            });


        }


        public boolean isPendingRemoval(int position) {
            GameProfile item = gameProfileList.get(position);
            return itemsPendingGameProfileList.contains(item);
        }


        class ViewHolder extends RecyclerView.ViewHolder {

            ImageView app_logo;
            TextView app_name;
            Context mContext;
            Button undoButton;


            public ViewHolder(View itemView, final Context context, int type) {
                super(itemView);
                mContext = context;
                if (type == 0) {
                    app_logo = (ImageView) itemView.findViewById(R.id.gameImage);
                    app_name = (TextView) itemView.findViewById(R.id.gameTitle);
                    undoButton = (Button) itemView.findViewById(R.id.undo_button);
                }
            }


        }


    }


}
