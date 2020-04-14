package com.eltech.CRS.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
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

            rowView = new ResultTableRow((ImageView) convertView.findViewById(R.id.carPic),
                                         (TextView) convertView.findViewById(R.id.carColor),
                                         (TextView) convertView.findViewById(R.id.carMark),
                                         (TextView) convertView.findViewById(R.id.carGovNum)
            );
            convertView.setTag(rowView);
        } else {
            rowView = (ResultTableRow) convertView.getTag();
        }

        CarInfo carInfo = (CarInfo) getItem(position);
        rowView.getCarPicView().setImageBitmap(carInfo.getPicture()); // set image
        rowView.getCarColorView().setText(carInfo.getColor());
        rowView.getCarMarkView().setText(carInfo.getMark());
        rowView.getCarGovNumView().setText(carInfo.getGovNum());

        return convertView;
    }

    public void setCarInfoList(List<CarInfo> carInfoList) {
        this.carInfoList = carInfoList;
    }
}
