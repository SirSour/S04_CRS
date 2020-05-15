package com.eltech.CRS.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.eltech.CRS.R;
import com.eltech.CRS.resultStuff.CarInfo;
import com.eltech.CRS.resultStuff.ResultTableRow;

import java.util.List;

public class ResultTableBodyAdapter extends BaseAdapter {
    private static final int CAR_COLOR_VISUAL_HEIGHT = 250;
    private static final int CAR_COLOR_VISUAL_WIDTH = 250;

    private Context tableBodyContext;
    private List<CarInfo> carInfoList;

    public ResultTableBodyAdapter(Context context, List<CarInfo> carInfoList) {
        tableBodyContext = context;
        this.carInfoList = carInfoList;
    }

    @Override
    public int getCount() {
        return carInfoList.size();
    }

    @Override
    public Object getItem(int position) {
        return carInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ResultTableRow rowView;

        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) tableBodyContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.sample_result_table_row, null);

            rowView = new ResultTableRow(convertView.findViewById(R.id.carPic),
                                         convertView.findViewById(R.id.carColorVisual),
                                         convertView.findViewById(R.id.carColor)
            );
            convertView.setTag(rowView);
        } else {
            rowView = (ResultTableRow) convertView.getTag();
        }

        CarInfo carInfo = (CarInfo) getItem(position);
        rowView.getCarPicView().setImageBitmap(carInfo.getPicture());
        rowView.getCarColorVisualView().setImageBitmap(makeBitmapFromColor(carInfo.getColor()));
        rowView.getCarColorView().setText(carInfo.getColorText());

        return convertView;
    }

    public void setCarInfoList(List<CarInfo> carInfoList) {
        this.carInfoList = carInfoList;
    }

    private Bitmap makeBitmapFromColor(int color) {
        Bitmap bitmap = Bitmap.createBitmap(CAR_COLOR_VISUAL_WIDTH, CAR_COLOR_VISUAL_HEIGHT, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(color);
        return bitmap;
    }
}
