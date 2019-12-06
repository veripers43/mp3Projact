package comassi.example.aiden.mymp3;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import static comassi.example.aiden.mymp3.MainActivity.myHelper;

public class MusicActivity extends AppCompatActivity implements View.OnClickListener {

    MyDBHelper myHelper;
    SQLiteDatabase sqlDB;
    MediaPlayer mediaPlayer;

    LinearLayout linLay;
    static TextView textView, title, singer, sbStart, sbEnd, tvInit;
    static ImageView album, play,like;
    static SeekBar seekBar;

    ImageView playList, mode, back, next;
    //처음에 한번만 들어왔다 나가기 위한 변수
    static boolean flag = true;
    //1=순차재생, 2=랜덤재생, 3=1곡반복
    static int abc = 1;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        //ㅇㅇ
        if (flag) {
            flag = false;
            finish();

        }

        linLay = findViewById(R.id.linLay);
        linLay.getBackground().setAlpha(150);
        tvInit = findViewById(R.id.tvInit);
        textView = findViewById(R.id.textView);
        title = findViewById(R.id.title);
        singer = findViewById(R.id.singer);
        album = findViewById(R.id.album);
        playList = findViewById(R.id.playList);
        mode = findViewById(R.id.mode);
        back = findViewById(R.id.back);
        play = findViewById(R.id.play);
        next = findViewById(R.id.next);
        seekBar = findViewById(R.id.seekBar);
        sbStart = findViewById(R.id.sbStart);
        sbEnd = findViewById(R.id.sbEnd);
        like = findViewById(R.id.like);

        textView.setSelected(true);
        title.setSelected(true);
        album.setSelected(true);


        playList.setOnClickListener(this);
        mode.setOnClickListener(this);
        back.setOnClickListener(this);
        play.setOnClickListener(this);
        next.setOnClickListener(this);

        like.setOnClickListener(this);


        //인텐트로 넘어올때 세팅값을 가져오지 않고 버튼을 눌렀을때 값을 가져옴
        //재생버튼을 두번 눌러서 값을 가져오고 상태유지
        MainActivity.btnPlay.callOnClick();
        MainActivity.btnPlay.callOnClick();
        myHelper = new MyDBHelper(this);

        //모드값도 초기화되기때문에 세번 눌러서 상태유지
        mode.callOnClick();
        mode.callOnClick();
        mode.callOnClick();

        mediaPlayer = new MediaPlayer();

        //seekbar가 터치되지 않도록
        seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        //앨범사진을 롱클릭하면 좋아요 저장된 데이터베이스가 초기화됨
        album.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                sqlDB = myHelper.getWritableDatabase();
                Toast.makeText(getApplicationContext(),"데이터베이스 초기화",Toast.LENGTH_SHORT).show();
                myHelper.onUpgrade(sqlDB, 1, 2);
                sqlDB.close();
                like.setImageResource(R.mipmap.likef);
                tvInit.setText("2");
                return false;
            }
        });

    }

    @Override
    public void onClick(View v) {
        sqlDB = myHelper.getWritableDatabase();

        switch (v.getId()) {
            case R.id.playList:
                finish();
                break;
            case R.id.mode:
                if (abc == 1) {
                    abc = 2;
                    mode.setImageResource(R.mipmap.shuffle);
                } else if (abc == 2) {
                    abc = 3;
                    mode.setImageResource(R.mipmap.one);
                } else if(abc == 3){
                    abc = 1;
                    mode.setImageResource(R.mipmap.repeat);
                }
                break;
            case R.id.back:
                MainActivity.btnBack.callOnClick();
                break;
            case R.id.play:
                MainActivity.btnPlay.callOnClick();
                break;
            case R.id.next:
                MainActivity.btnNext.callOnClick();
                break;

            case R.id.like:

                //좋아요가 아니면 데이터베이스에 등록
                if(tvInit.getText().equals("2")){
                    sqlDB.execSQL("INSERT INTO musicTBL VALUES ('"+
                            title.getText().toString()+"');");
                    sqlDB.close();
                    tvInit.setText("1");
                    like.setImageResource(R.mipmap.liket);
                    break;

                    //좋아요면 데이터베이스에서 삭제
                }else if(tvInit.getText().equals("1")){
                    sqlDB.execSQL("DELETE FROM musicTBL WHERE title = '"
                            + title.getText().toString() + "';");
                    sqlDB.close();
                    tvInit.setText("2");

                    like.setImageResource(R.mipmap.likef);
                    break;
                }




        }

    }
}
