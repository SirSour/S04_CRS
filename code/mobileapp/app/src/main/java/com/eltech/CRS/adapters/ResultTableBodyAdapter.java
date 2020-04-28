package com.eltech.CRS.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.eltech.CRS.R;
import com.eltech.CRS.resultStuff.CarInfo;
import com.eltech.CRS.resultStuff.ResultTableRow;

import java.util.List;

public class ResultTableBodyAdapter extends BaseAdapter {
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
                                         convertView.findViewById(R.id.carColor),
                                         convertView.findViewById(R.id.carMark),
                                         convertView.findViewById(R.id.carGovNum)
            );
            convertView.setTag(rowView);
        } else {
            rowView = (ResultTableRow) convertView.getTag();
        }

        CarInfo carInfo = (CarInfo) getItem(position);
        rowView.getCarPicView().setImageBitmap(carInfo.getPicture());
        rowView.getCarColorVisualView().setImageBitmap(makeBitmapFromColor(carInfo.getColor()));
        rowView.getCarColorView().setText(carInfo.getColorText());
        rowView.getCarMarkView().setText(carInfo.getMark());
        rowView.getCarGovNumView().setText(carInfo.getGovNum());

        return convertView;
    }

    public void setCarInfoList(List<CarInfo> carInfoList) {
        this.carInfoList = carInfoList;
    }

    private Bitmap makeBitmapFromColor(Color color) {
        Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(color.toArgb());
//        bitmap.eraseColor(Color.argb(255, 255, 0, 0));
        return bitmap;
    }
}
