package com.seazon.fo.activity;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.seazon.fo.AppUtil;
import com.seazon.fo.Core;
import com.seazon.fo.Helper;
import com.seazon.fo.ImageUtil;
import com.seazon.fo.MediaThumbCallback;
import com.seazon.fo.R;
import com.seazon.fo.SupportUtils;
import com.seazon.fo.VideoUtil;
import com.seazon.fo.view.selector.FoSelector;
import com.seazon.utils.LogUtils;

public class FileAdapter extends BaseAdapter implements MediaThumbCallback {

    private LayoutInflater inflater;
    private BaseActivity activity;
    private Core core;
    private AbsListView listView;
    private List<Map<String, Object>> data;
    private String viewType;
    private SparseArray<SoftReference<Bitmap>> iconCache = new SparseArray<SoftReference<Bitmap>>();
    private SparseBooleanArray realBig = new SparseBooleanArray();

    public FileAdapter(BaseActivity activity, List<Map<String, Object>> data, String viewType, AbsListView listView) {
        super();
        this.data = data;
        this.activity = activity;
        this.core = (Core) activity.getApplication();
        this.inflater = LayoutInflater.from(activity);
        this.viewType = viewType;
        this.listView = listView;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int arg0) {
        return data.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    public static final String FILE_PATH = "filePath";
    public static final String NAME = "name";
    public static final String DESC = "desc";
    public static final String RES_ID = "resId";
    public static final String SELECT = "select";

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        long tt = System.currentTimeMillis();
        ViewHolder holder = null;
        if (convertView == null) {

            holder = new ViewHolder();
            holder.viewType = viewType;

            if (viewType.equals(Core.VIEW_LIST)) {
                convertView = inflater.inflate(R.layout.filelist_row_tiles, null);
                holder.desc = (TextView) convertView.findViewById(R.id.desc);
            } else if (viewType.equals(Core.VIEW_ICONS)) {
                convertView = inflater.inflate(R.layout.filelist_row_icons, null);
                holder.desc = null;
            }
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.img = (ImageView) convertView.findViewById(R.id.img);
            holder.select = (ImageView) convertView.findViewById(R.id.select);

            convertView.setTag(holder);

            View bgLayout = convertView.findViewById(R.id.bgLayout);
            SupportUtils.setBackground(bgLayout, FoSelector.normal(activity));

        } else {

            holder = (ViewHolder) convertView.getTag();

        }

        HashMap<String, Object> map = (HashMap<String, Object>) data.get((int) position);
        String name = (String) map.get(NAME);
        String desc = (String) map.get(DESC);
        String filePaht = (String) map.get(FILE_PATH);
        int select = (Integer) map.get(SELECT);
        int resId = (Integer) map.get(RES_ID);
        if (holder.name != null) {
            if (name == null || name.equals("")) {
                holder.name.setVisibility(View.GONE);
            } else {
                holder.name.setVisibility(View.VISIBLE);
                holder.name.setText(name);
            }
        }
        if (holder.desc != null) {
            if (desc == null || desc.equals("")) {
                holder.desc.setVisibility(View.GONE);
            } else {
                holder.desc.setVisibility(View.VISIBLE);
                holder.desc.setText(desc);
            }
        }

        holder.img.setTag(filePaht + "img");
        holder.name.setTag(filePaht + "name");
        holder.img.setImageBitmap(getIcon(position, filePaht, resId));

        if (viewType.equals(Core.VIEW_ICONS) && realBig.get(position)) {
            holder.name.setVisibility(View.GONE);
            holder.img.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
            holder.img.getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;
            ((RelativeLayout.LayoutParams) holder.img.getLayoutParams()).topMargin = 0;
        } else {
            holder.name.setVisibility(View.VISIBLE);
            holder.img.getLayoutParams().width = core.du.dip2px(48);
            holder.img.getLayoutParams().height = core.du.dip2px(48);
            ((RelativeLayout.LayoutParams) holder.img.getLayoutParams()).topMargin = core.du.dip2px(4);
        }

        if (core.mode == Core.MODE_SELECT) {
            if (select == 1) {
                holder.select.setImageDrawable(FoSelector.select(core));
                holder.select.setVisibility(View.VISIBLE);
            } else {
                holder.select.setImageDrawable(null);
                holder.select.setVisibility(View.VISIBLE);
            }
        } else {
            holder.select.setVisibility(View.GONE);
        }
        if (System.currentTimeMillis() - tt > a)
            LogUtils.debug("  getView5 cost" + (System.currentTimeMillis() - tt));
        return convertView;
    }

    private Bitmap getIcon(final int position, String filePath, int resId) {
        long tt = System.currentTimeMillis();
        Bitmap bitmap = null;

        if (position >= 0 && position < iconCache.size() && iconCache.get(position) != null) {
            bitmap = iconCache.get(position).get();

        }
        String ext = Helper.getTypeByExtension(filePath);
        final File file = new File(filePath);
        if (bitmap == null) {
            bitmap = FileIconCache.getIcon(file.hashCode());
            if (bitmap != null) {
                iconCache.put((int) position, new SoftReference<Bitmap>(bitmap));
                if (ext.startsWith("image/") || ext.startsWith("video/")) {
                    realBig.put((int) position, true);
                }
            } else {
                FileIconCache.setShowThumb(file.hashCode(), false);
            }
        }
        if (bitmap == null) {

            if (ext.startsWith("image/") || ext.startsWith("video/")) {
                realBig.put((int) position, false);
            }
            if (resId != R.drawable.format_folder && resId != R.drawable.format_folder_dark
                    && resId != R.drawable.format_folder_lock && resId != R.drawable.format_folder_lock_dark) {
                // if(file.isFile()){
                if (core.getMainPreferences().isShowThumb()) {
                    if (ext.startsWith("image/")) {

                        new Thread() {
                            @Override
                            public void run() {
                                Bitmap bitmap = ImageUtil.getThumb2(position, file,
                                        viewType.equals(Core.VIEW_ICONS) ? 2 : 1, core);
                                getImageAndSaveCacheAndSendMessage(position, bitmap);
                            };
                        }.start();

                    } else if (ext.startsWith("video/")) {
                        VideoUtil.getThumb(activity, file, position, viewType.equals(Core.VIEW_ICONS) ? 2 : 1,
                                new MediaThumbCallback() {
                                    @Override
                                    public void callback(long position, Bitmap bitmap) {
                                        getImageAndSaveCacheAndSendMessage(position, bitmap);
                                    }
                                });
                    } else if (ext.equals("application/vnd.android.package-archive")) {
                        AppUtil.getThumb(position, file, activity, new MediaThumbCallback() {
                            @Override
                            public void callback(long position, Bitmap bitmap) {
                                getImageAndSaveCacheAndSendMessage(position, bitmap);
                            }
                        });
                    }
                }
            }

            bitmap = FileIconCache.getDefaultIcon(resId, activity.getResources());
            iconCache.put(position, new SoftReference<Bitmap>(bitmap));
        }
        if (System.currentTimeMillis() - tt > a)
            LogUtils.debug("  getIcon3 cost" + (System.currentTimeMillis() - tt));
        return bitmap;
    }

    public void getImageAndSaveCacheAndSendMessage(long position, Bitmap bitmap) {

        if (bitmap == null)
            return;

        if (data == null || data.size() <= position) {
            return;
        }

        // FIXME java.lang.IndexOutOfBoundsException
        HashMap<String, Object> map = (HashMap<String, Object>) data.get((int) position);
        File file = new File((String) map.get(FILE_PATH));

        iconCache.put((int) position, new SoftReference<Bitmap>(bitmap));
        FileIconCache.putIcon(file.hashCode(), bitmap);

        String ext = Helper.getTypeByExtension(file.getPath());
        if (ext.startsWith("image/") || ext.startsWith("video/")) {
            realBig.put((int) position, true);
            FileIconCache.setShowThumb(file.hashCode(), true);
        }

        Message message = mHandler.obtainMessage();
        Object[] os = new Object[2];
        os[0] = position;
        os[1] = bitmap;
        message.obj = os;
        message.what = 1;
        mHandler.sendMessage(message);
    }

    public void callback2(int fileHashcode, Bitmap bitmap) {
        LogUtils.debug("position:" + fileHashcode);
        long tt = System.currentTimeMillis();

        ImageView imageViewByTag = (ImageView) listView.findViewWithTag(fileHashcode + "img");
        TextView nameViewByTag = (TextView) listView.findViewWithTag(fileHashcode + "name");
        LogUtils.debug("imageViewByTag:" + imageViewByTag + ", nameViewByTag:" + imageViewByTag);

        if (viewType.equals(Core.VIEW_ICONS) && realBig.get(fileHashcode)) {

            if (nameViewByTag != null) {
                nameViewByTag.setVisibility(View.GONE);
            }
            if (imageViewByTag != null) {
                imageViewByTag.setImageBitmap(bitmap);
                imageViewByTag.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
                imageViewByTag.getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;
                ((RelativeLayout.LayoutParams) imageViewByTag.getLayoutParams()).topMargin = 0;
            }
        } else {
            if (nameViewByTag != null) {
                nameViewByTag.setVisibility(View.VISIBLE);
            }
            if (imageViewByTag != null) {
                imageViewByTag.setImageBitmap(bitmap);
                imageViewByTag.getLayoutParams().width = core.du.dip2px(48);
                imageViewByTag.getLayoutParams().height = core.du.dip2px(48);
                ((RelativeLayout.LayoutParams) imageViewByTag.getLayoutParams()).topMargin = core.du.dip2px(4);
            }
        }

        if (System.currentTimeMillis() - tt > a)
            LogUtils.debug("  callback cost" + (System.currentTimeMillis() - tt));
    }

    @Override
    public void callback(long position, Bitmap bitmap) {
        LogUtils.debug("position:" + position);
        long tt = System.currentTimeMillis();

        if (data == null || data.size() <= position) {
            return;
        }
        
        HashMap<String, Object> map = (HashMap<String, Object>) data.get((int) position);
        File file = new File((String) map.get(FILE_PATH));

        String ext = Helper.getTypeByExtension(file.getPath());
        if (ext.startsWith("image/") || ext.startsWith("video/")) {
            realBig.put((int) position, true);
            FileIconCache.setShowThumb(file.hashCode(), true);
        }

        ImageView imageViewByTag = (ImageView) listView.findViewWithTag((String) map.get(FILE_PATH) + "img");
        TextView nameViewByTag = (TextView) listView.findViewWithTag((String) map.get(FILE_PATH) + "name");

        if (viewType.equals(Core.VIEW_ICONS) && realBig.get((int) position)) {

            if (nameViewByTag != null) {
                nameViewByTag.setVisibility(View.GONE);
            }
            if (imageViewByTag != null) {
                imageViewByTag.setImageBitmap(bitmap);
                imageViewByTag.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
                imageViewByTag.getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;
                ((RelativeLayout.LayoutParams) imageViewByTag.getLayoutParams()).topMargin = 0;
            }
        } else {
            if (nameViewByTag != null) {
                nameViewByTag.setVisibility(View.VISIBLE);
            }
            if (imageViewByTag != null) {
                imageViewByTag.setImageBitmap(bitmap);
                imageViewByTag.getLayoutParams().width = core.du.dip2px(48);
                imageViewByTag.getLayoutParams().height = core.du.dip2px(48);
                ((RelativeLayout.LayoutParams) imageViewByTag.getLayoutParams()).topMargin = core.du.dip2px(4);
            }
        }

        if (System.currentTimeMillis() - tt > a)
            LogUtils.debug("  callback cost" + (System.currentTimeMillis() - tt));
    }

    /**
     * 因为要重设view，getView(position, view, listView);，不建议使用
     * 
     * @param position
     * @param select
     *            0 not select, 1 select, -1 not change
     */
    @Deprecated
    public void updateView(int position, int select) {
        long tt = System.currentTimeMillis();
        if (select != -1) {
            HashMap<String, Object> map = (HashMap<String, Object>) data.get((int) position);
            map.put(SELECT, select);
        }
        int visiblePosition = listView.getFirstVisiblePosition();
        View view = listView.getChildAt(position - visiblePosition);
        if (System.currentTimeMillis() - tt > a)
            LogUtils.debug("updateView cost" + (System.currentTimeMillis() - tt));
        getView(position, view, listView);
    }

    public static int a = 10;

    static class ViewHolder {
        String viewType;
        ImageView img;
        TextView name;
        TextView desc;
        ImageView select;
    }

    public static final int MEDIA_THUMB_CALLBACK_APP = 1;
    public static final int MEDIA_THUMB_CALLBACK_IAMGE = 2;
    public static final int MEDIA_THUMB_CALLBACK_VIDEO = 3;
    public static final int MEDIA_THUMB_CALLBACK_AUDIO = 4;

    final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case 1:
                Object[] a = (Object[]) msg.obj;
                long position = (Long) a[0];
                Bitmap bitmap = (Bitmap) a[1];
                callback(position, bitmap);
                break;
            }
        }
    };
    final Handler mHandler2 = new Handler() {
        public void handleMessage(Message msg) {
            callback2(msg.arg1, (Bitmap) msg.obj);
        }
    };

}
