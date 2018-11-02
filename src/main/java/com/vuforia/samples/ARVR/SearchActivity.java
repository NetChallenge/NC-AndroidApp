package com.vuforia.samples.ARVR;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.vuforia.samples.model.Room;
import com.vuforia.samples.model.User;
import com.vuforia.samples.network.NCARApiRequest;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener{
    private SearchView searchView;
    private ListView selectedUserView;
    private ListView listView;
    private ArrayList<User> selectedUserList = new ArrayList<>();
    private ArrayList<User> userList;
    private SelectedUserAdapter selectedUserAdapter;
    private SearchUserAdapter userAdapter;
    private NCARProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        dialog = new NCARProgressDialog(this);

        searchView = findViewById(R.id.search_searchview);
        selectedUserView = findViewById(R.id.search_selected_listview);
        listView = findViewById(R.id.search_listview);

        selectedUserAdapter = new SelectedUserAdapter(SearchActivity.this, selectedUserList);
        selectedUserView.setAdapter(selectedUserAdapter);

        userAdapter = new SearchUserAdapter(SearchActivity.this, userList);
        listView.setAdapter(userAdapter);

        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(this);
        searchView.setSubmitButtonEnabled(true);
        searchView.setQueryHint("이름으로 검색...");

        findViewById(R.id.search_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedUserList.size() == 0)
                    Toast.makeText(SearchActivity.this, "최소 1명이상 선택해주세요.", Toast.LENGTH_SHORT).show();
                else {
                    Intent intent = new Intent();
                    intent.putExtra("users", selectedUserList);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
    }

    @Override
    public boolean onQueryTextSubmit(final String query) {
        dialog.showProgressDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Pair<NCARApiRequest.NCARApi_Err, ArrayList<User>> err = NCARApiRequest.searchUser(query);
                switch (err.first) {
                    case SUCCESS:
                        userList = err.second;
                        for(int i=0; i<userList.size(); i++)
                            if(userList.get(i).getUserEmail() == User.currentUser.getUserEmail()) {
                                userList.remove(i);
                                break;
                            }

                        userAdapter.setList(userList);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                userAdapter.notifyDataSetChanged();
                            }
                        });
                        break;

                    case NETWORK_ERR:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SearchActivity.this, R.string.ncar_network_err, Toast.LENGTH_LONG).show();
                            }
                        });
                        break;
                    case UNKNOWN_ERR:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SearchActivity.this, R.string.ncar_unknown_err, Toast.LENGTH_LONG).show();
                            }
                        });
                        break;
                }
                dialog.hideProgressDialog();
            }
        }).start();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    private class SelectedUserAdapter extends BaseAdapter {
        private Context context;
        private ArrayList<User> userArrayList;

        public SelectedUserAdapter(Context context, ArrayList<User> userArrayList) {
            this.context = context;
            this.userArrayList = userArrayList;
        }

        private class SearchUserHolder {
            ImageView checkView;
            TextView nameView;
            TextView emailView;
        }

        public void setList(ArrayList<User> userArrayList) {
            this.userArrayList = userArrayList;
        }

        @Override
        public int getCount() {
            if(userArrayList == null)
                return 0;
            else
                return userArrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return userArrayList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            SearchUserHolder holder;
            if(convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.search_list_item, parent,false);
                holder = new SearchUserHolder();
                holder.checkView = convertView.findViewById(R.id.search_list_check);
                holder.nameView = convertView.findViewById(R.id.search_list_name);
                holder.emailView = convertView.findViewById(R.id.search_list_email);
                convertView.setTag(holder);
            }
            else
                holder = (SearchUserHolder)convertView.getTag();

            holder.nameView.setText(userArrayList.get(position).getUserName());
            holder.emailView.setText(userArrayList.get(position).getUserEmail());

            return convertView;
        }
    }

    private class SearchUserAdapter extends BaseAdapter {
        private Context context;
        private ArrayList<User> userArrayList;

        public SearchUserAdapter(Context context, ArrayList<User> userArrayList) {
            this.context = context;
            this.userArrayList = userArrayList;
        }

        private class SearchUserHolder {
            ImageView checkView;
            TextView nameView;
            TextView emailView;
        }

        public void setList(ArrayList<User> userArrayList) {
            this.userArrayList = userArrayList;
        }

        @Override
        public int getCount() {
            if(userArrayList == null)
                return 0;
            else
                return userArrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return userArrayList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            SearchUserHolder holder;
            if(convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.search_list_item, parent,false);
                holder = new SearchUserHolder();
                holder.checkView = convertView.findViewById(R.id.search_list_check);
                holder.nameView = convertView.findViewById(R.id.search_list_name);
                holder.emailView = convertView.findViewById(R.id.search_list_email);
                convertView.setTag(holder);
            }
            else
                holder = (SearchUserHolder)convertView.getTag();

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SearchUserHolder holder = (SearchUserHolder)v.getTag();
                    if(holder.checkView.getVisibility() == View.GONE) {
                        selectedUserList.add(userArrayList.get(position));
                        selectedUserAdapter.notifyDataSetChanged();
                        holder.checkView.setVisibility(View.VISIBLE);
                        notifyDataSetChanged();
                    }
                    else if(holder.checkView.getVisibility() == View.VISIBLE) {
                        selectedUserList.remove(userArrayList.get(position));
                        selectedUserAdapter.notifyDataSetChanged();
                        holder.checkView.setVisibility(View.GONE);
                        notifyDataSetChanged();
                    }
                }
            });
            holder.nameView.setText(userArrayList.get(position).getUserName());
            holder.emailView.setText(userArrayList.get(position).getUserEmail());

            return convertView;
        }
    }
}
