package com.seazon.fo.menu;

import java.io.File;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.EditText;
import android.widget.Toast;

import com.seazon.fo.Core;
import com.seazon.fo.Favorite;
import com.seazon.fo.FavoritesConfig;
import com.seazon.fo.R;
import com.seazon.fo.RefreshType;
import com.seazon.fo.activity.FoSlideActivity;
import com.seazon.fo.listener.RefreshListener;

public class AddFavorriteMenu extends SingleFileAction {

    public static final int FAVORITE_MAX = 10;

    public AddFavorriteMenu(int id, int type, RefreshListener listener, FoSlideActivity activity) {
        super(id, type, listener, activity);
    }

    @Override
    public void onActive() {
        // "add favorites"

        final File file = core.getClipper().getCopys().get(0);
        if (!file.isDirectory()) {
            Toast.makeText(context, R.string.operator_add_favorites_failed, Toast.LENGTH_SHORT).show();

            return;
        }

        List<Favorite> list = FavoritesConfig.getFavorites();
        if (list != null && list.size() >= FAVORITE_MAX) {
            Toast.makeText(context,
                    context.getString(R.string.operator_add_favorites_failed_more_than_max, FAVORITE_MAX),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder abc = new AlertDialog.Builder(context);
        abc.setTitle(R.string.operator_add_favorites);
        final EditText input22 = new EditText(context);
        input22.setSingleLine();
        input22.setText(file.getName());
        input22.selectAll();
        abc.setView(input22);
        abc.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Favorite f = new Favorite();
                f.name = input22.getText().toString();
                f.path = file.getPath();
                FavoritesConfig.saveFavorite(f);

                activity.onRefreshSide();
                Toast.makeText(context, R.string.operator_add_favorites_successful, Toast.LENGTH_SHORT).show();
            }
        }).setNegativeButton(android.R.string.cancel, null);
        AlertDialog dd = abc.create();
        dd.setCanceledOnTouchOutside(true);
        dd.show();

        listener.onRefresh(true, Core.MODE_NORMAL, RefreshType.SELECT_RESET, true);
    }

    @Override
    protected int getIconForInit() {
        return R.drawable.ic_menu_favorites;
    }

    @Override
    protected int getNameForInit() {
        return R.string.operator_add_favorites;
    }

}
