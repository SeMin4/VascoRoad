package com.example.woo.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class MyRoomListAdapter extends BaseAdapter implements View.OnClickListener{
    public interface ListBtnClickListener{
        void onListBtnClick(int position);
    }
    public ArrayList<MyRoomItem> myRoomList = new ArrayList<MyRoomItem>();
    private ListBtnClickListener listBtnClickListener;

    public MyRoomListAdapter(ListBtnClickListener listBtnClickListener){
        this.listBtnClickListener = listBtnClickListener;
    }
    public MyRoomListAdapter(){

    }
    @Override
    public int getCount(){
        return myRoomList.size();
    }

    @Override
    public View getView(int position, View convertView , ViewGroup parent){
        final int pos = position;
        final Context context = parent.getContext();

        // "my_room_listview" Layout inflate convertView
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.my_room_listview, parent, false);
        }

        TextView titlePersonTextView = (TextView) convertView.findViewById(R.id.title_mPerson) ;
        TextView locationMapTextView = (TextView) convertView.findViewById(R.id.location_map) ;
        Button myRoomDeleteButton = (Button) convertView.findViewById(R.id.my_room_delete);
        // Data Set(myRoomList)에서 position 에 위치한 데이터 참조 획득
        MyRoomItem RoomItem = myRoomList.get(position);

        // 아이템 내 각 위젯에 데이터 반영
        titlePersonTextView.setText(RoomItem.getP_name());
        locationMapTextView.setText(RoomItem.getM_place_string());
        myRoomDeleteButton.setTag(position);
        myRoomDeleteButton.setOnClickListener(this);
        return convertView;

    }
    @Override
    public long getItemId(int position){
        return position;
    }
    @Override
    public Object getItem(int position){
        return myRoomList.get(position);
    }

    public void addItem(String m_id,String titlePerson, String location_map){
        MyRoomItem item = new MyRoomItem();

        item.setM_id(m_id);
        item.setP_name(titlePerson);
        item.setM_place_string(location_map);

        myRoomList.add(item);
    }

    public void onClick(View v){
        //MyPageActivity 의 onlistBtnClick()함수 호출
        if(this.listBtnClickListener != null){
            this.listBtnClickListener.onListBtnClick((int)v.getTag());
        }
    }

}
