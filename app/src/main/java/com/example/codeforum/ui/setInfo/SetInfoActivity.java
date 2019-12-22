package com.example.codeforum.ui.setInfo;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.codeforum.R;
import com.example.codeforum.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SetInfoActivity extends AppCompatActivity {
    private SharedPreferences user;
    private String user_phone;
    private ActionBar actionBar;
    private ImageView set_info_back;
    private EditText set_info_my_name;
    private EditText set_info_birth_date;
    private RadioGroup set_info_gender;
    private RadioButton set_info_gender_man;
    private RadioButton set_info_gender_woman;
    private Button set_info;
    //private EditText set_info_hometown;
    private Spinner set_info_hometown;
    private String gender_info = null;
    private String hometown_info=null;
    private List<String> list;
    private ArrayAdapter<String> adapter;
    private static String cityJson = "{'cities':['','上海','北京','杭州','广州','南京','苏州','深圳','成都','重庆','天津','宁波','扬州','无锡','福州','厦门','武汉','西安','沈阳','大连','青岛','济南','海口','石家庄','唐山','秦皇岛','邯郸','邢台','保定','张家口','承德','沧州','廊坊','衡水','太原','大同','阳泉','长治','晋城','朔州','晋中','运城','忻州','临汾','吕梁','呼和浩特','包头','乌海','赤峰','通辽','鄂尔多斯','呼伦贝尔','兴安盟','锡林郭勒','乌兰察布','巴彦淖尔','阿拉善','鞍山','抚顺','本溪','丹东','锦州','营口','阜新','辽阳','盘锦','铁岭','朝阳','葫芦岛','长春','吉林','四平','辽源','通化','白山','松原','白城','延边','哈尔滨','齐齐哈尔','鸡西','鹤岗','双鸭山','大庆','伊春','佳木斯','七台河','牡丹江','黑河','绥化','大兴安岭','徐州','常州','南通','连云港','淮安','盐城','镇江','泰州','宿迁','温州','嘉兴','湖州','绍兴','金华','衢州','舟山','台州','丽水','合肥','芜湖','蚌埠','淮南','马鞍山','淮北','铜陵','安庆','黄山','滁州','阜阳','宿州','六安','亳州','池州','宣城','莆田','三明','泉州','漳州','南平','龙岩','宁德','南昌','景德镇','萍乡','九江','新余','鹰潭','赣州','吉安','宜春','抚州','上饶','淄博','枣庄','东营','烟台','潍坊','济宁','泰安','威海','日照','莱芜','临沂','德州','聊城','滨州','菏泽','郑州','开封','洛阳','平顶山','安阳','鹤壁','新乡','焦作','濮阳','许昌','漯河','三门峡','南阳','商丘','信阳','周口','驻马店','黄石','十堰','宜昌','襄阳','鄂州','荆门','孝感','荆州','黄冈','咸宁','随州','恩施州','仙桃','潜江','天门','株洲','湘潭','衡阳','邵阳','岳阳','常德','张家界','益阳','郴州','永州','怀化','娄底','湘西','韶关','珠海','汕头','佛山','江门','湛江','茂名','肇庆','惠州','梅州','汕尾','河源','阳江','清远','东莞','中山','潮州','揭阳','云浮','南宁','柳州','桂林','梧州','北海','防城港','钦州','贵港','玉林','百色','贺州','河池','自贡','攀枝花','泸州','德阳','绵阳','广元','遂宁','内江','乐山','南充','眉山','宜宾','广安','达州','雅安','巴中','资阳','阿坝','甘孜州','凉山','贵阳','六盘水','遵义','安顺','铜仁地区','黔西南','毕节地区','黔东南','黔南','昆明','曲靖','玉溪','保山','昭通','楚雄州','红河','文山州','普洱','西双版纳','大理州','德宏','丽江','怒江','迪庆','临沧','拉萨','昌都地区','山南','日喀则地区','那曲','阿里','林芝地区','铜川','宝鸡','咸阳','渭南','延安','汉中','榆林','安康','商洛','兰州','嘉峪关','金昌','白银','天水','武威','张掖','平凉','酒泉','庆阳','定西','陇南','临夏州','甘南','西宁','海东','海北','黄南','果洛','玉树','海西','银川','石嘴山','吴忠','固原','乌鲁木齐','克拉玛依','吐鲁番地区','哈密地区','昌吉州','博尔塔拉','巴音郭楞','阿克苏地区','克孜勒苏','喀什地区','和田地区','伊犁','塔城地区','阿勒泰地区','石河子','香港','澳门','长沙','三亚','中卫','儋州','保亭','昌江','澄迈县','崇左','定安县','东方','济源','来宾','乐东','陵水','琼海','神农架林区','图木舒克','屯昌县','万宁','文昌','海南州']}";
    private SetInfoHandler handler = new SetInfoHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_info);
        actionBar=getSupportActionBar();
        actionBar.hide();

        set_info_back = (ImageView) findViewById(R.id.set_info_back);
        set_info_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri data = null;
                Intent intent = new Intent(null, data);
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });

        set_info_my_name=(EditText) findViewById(R.id.set_info_my_name);
        set_info_birth_date=(EditText) findViewById(R.id.set_info_birth_date);
        set_info_gender=(RadioGroup) findViewById(R.id.set_info_gender);
        set_info_gender_man=(RadioButton) findViewById(R.id.set_info_gender_man);
        set_info_gender_woman=(RadioButton) findViewById(R.id.set_info_gender_woman);
        //set_info_hometown=(EditText) findViewById(R.id.set_info_hometown);
        set_info_hometown=(Spinner) findViewById(R.id.set_info_hometown);
        set_info=(Button)findViewById(R.id.set_info);

        set_info_gender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.set_info_gender_man:
                        gender_info = set_info_gender_man.getText().toString().trim();
                        break;
                    case R.id.set_info_gender_woman:
                        gender_info = set_info_gender_woman.getText().toString().trim();
                        break;
                    default:
                        break;
                }
            }
        });

        set_info_birth_date.setText("1998" + "-" + "4" + "-" + "22");
        set_info_birth_date.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showDatePickDlg();
                }
            }
        });

        list = new ArrayList();
        try
        {
            JSONObject jo1 = new JSONObject(cityJson);
            JSONArray ja1 = jo1.getJSONArray("cities");
            for(int i = 0; i < ja1.length(); i++)
            {
                String cityName = ja1.getString(i);
                list.add(cityName);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        adapter=new ArrayAdapter<String> (this,R.layout.my_spinner,list);
        adapter.setDropDownViewResource(R.layout.my_spinner);
        set_info_hometown.setAdapter(adapter);
        set_info_hometown.setOnItemSelectedListener(new spinnerSelectedListenner());//绑定事件监听

        set_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int status=0;
                        try{
                            status=updateInfo();
                        }catch(IOException e){
                            e.printStackTrace();
                        }
                        if(status==1){
                            Looper.prepare();
                            Toast.makeText(SetInfoActivity.this, "成功更新您的个人信息！", Toast.LENGTH_LONG).show();
                            Looper.loop();
                        }else{
                            Looper.prepare();
                            Toast.makeText(SetInfoActivity.this, "更新您的个人信息的过程中发生错误！", Toast.LENGTH_LONG).show();
                            Looper.loop();
                        }
                    }
                }).start();
            }
        });

        user = getSharedPreferences("user", 0);
        if (user != null) {
            user_phone = user.getString("user_phone", "默认值");
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{

                    getInfo();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private class spinnerSelectedListenner implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int position, long id) {
            // TODO Auto-generated method stub
            hometown_info = parent.getItemAtPosition(position).toString();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // TODO Auto-generated method stub

        }
    }

    private void showDatePickDlg() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(SetInfoActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                SetInfoActivity.this.set_info_birth_date.setText(year + "-" + monthOfYear + "-" + dayOfMonth);
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void getInfo()throws IOException{
        Bundle bundle = new Bundle();
        if (user_phone!=null&&!user_phone.equals("默认值")) {
            String urlstr = "http://58.87.100.195/CodeForum/getInfo.php";
            String params = "phone=" + user_phone;
            InputStream is = Utils.connect(urlstr, params);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));//获得输入流
            String line = "";
            StringBuilder sb = new StringBuilder();
            while (null != (line = bufferedReader.readLine())) {
                sb.append(line);
            }
            String result = sb.toString();
            try {
                JSONObject jsonObject = new JSONObject(result);
                int _status = jsonObject.getInt("status");
                if(_status==1) {
                    String _name = jsonObject.getString("name");
                    String _birth_date = jsonObject.getString("birth_date");
                    String _gender = jsonObject.getString("gender");
                    String _hometown = jsonObject.getString("hometown");
                    bundle.putString("name", _name);
                    bundle.putString("birth_date", _birth_date);
                    bundle.putString("gender", _gender);
                    bundle.putString("hometown", _hometown);
                    Message msg = Message.obtain();
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }else{
                    Log.e("getInfoError:","获取信息失败！" );
                }
            } catch (Exception e) {
                Log.e("log_tag", "the Error parsing data " + e.toString());
            }
        }
    }

    private int updateInfo()throws IOException {
        int returnResult=0;
        String my_name_info=set_info_my_name.getText().toString();
        String birth_date_info=set_info_birth_date.getText().toString();
        String urlstr = "http://58.87.100.195/CodeForum/setInfo.php";
        if (user_phone!=null&&!user_phone.equals("默认值")) {
            String params = "phone=" + user_phone + '&' + "name=" + my_name_info + '&' + "birth_date=" + birth_date_info + '&' + "gender=" + gender_info + '&' + "hometown=" + hometown_info;
            InputStream is= Utils.connect(urlstr,params);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
            String line = "";
            StringBuilder sb = new StringBuilder();
            while (null != (line = bufferedReader.readLine())) {
                sb.append(line);
            }
            String result = sb.toString();
            try {
                JSONObject jsonObject = new JSONObject(result);
                returnResult = jsonObject.getInt("status");
            } catch (Exception e) {
                Log.e("log_tag", "the Error parsing data " + e.toString());
            }
        }
        if(returnResult==1){
            SharedPreferences sharedPreferences =getSharedPreferences("user", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("user_id", my_name_info);
            editor.apply();
        }
        return returnResult;
    }

    private class SetInfoHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            String _name = msg.getData().getString("name");
            String _birth_date = msg.getData().getString("birth_date");
            String _gender = msg.getData().getString("gender");
            String _hometown = msg.getData().getString("hometown");
            set_info_my_name.setText(_name);
            if(!_birth_date.equals("null")){
                set_info_birth_date.setText(_birth_date);
            }else{
                set_info_birth_date.setText("");
            }
            if(_gender.equals("man")){
                set_info_gender_man.setChecked(true);
            }else if(_gender.equals("woman")){
                set_info_gender_woman.setChecked(true);
            }
            if(!_hometown.equals("null")&&_hometown.length()>0){
                set_info_hometown.setSelection(list.indexOf(_hometown));
            }else{
                set_info_hometown.setSelection(0);
            }
        }
    }
}
