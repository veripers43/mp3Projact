package comassi.example.aiden.mymp3;


import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    //잡것들
    static boolean like = true; //좋아요
    int plus;                   //모드관련
    ConstraintLayout conLay;

    //리사이클러뷰
    LinearLayoutManager linearLayoutManager;
    MainAdapter mainAdapter;
    ArrayList<MainData> dataList = new ArrayList<MainData>();
    RecyclerView recyclerViewMP3;


    //데이터에이스
    static MyDBHelper myHelper;
    static SQLiteDatabase sqlDB;
    Cursor cursor;

    //인텐트
    static int now = 0;          //현재 재생중인 노래 순서
    static boolean check = true; //같은곡, 다른곡, 다른곡이면 true;
    Boolean flag = true;          //재생중인가 정지중이면 true;
    private ContentResolver res;


    //메인
    static ImageButton btnPlay, btnNext, btnBack;
    LinearLayout linearLayout;
    ImageView imageView;
    TextView tvTitle, tvSinger;
    static SeekBar pbMP3;
    MediaPlayer mediaPlayer;
    int playbackPosition = 0;      //재생위치
    private long time= 0;         //종료시간


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //음악파일의 내용이 리스트에 들어옴
        getMusicList();

        Log.d("start"," 작업시작");

        //데이터 관련
        res = getContentResolver();
        mediaPlayer = new MediaPlayer();
        myHelper = new MyDBHelper(this);


        linearLayout = findViewById(R.id.linearLayout);
        conLay = findViewById(R.id.conLay);
        conLay.getBackground().setAlpha(120);
        recyclerViewMP3 = findViewById(R.id.recyclerViewMP3);

        //아답터 설정
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerViewMP3.setLayoutManager(linearLayoutManager);
        mainAdapter = new MainAdapter(dataList, this);
        recyclerViewMP3.setAdapter(mainAdapter);


        btnPlay = findViewById(R.id.btnPlay);
        btnBack = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);
        tvTitle = findViewById(R.id.tvTitle);
        tvSinger = findViewById(R.id.tvSinger);
        pbMP3 = findViewById(R.id.pbMP3);

        imageView = findViewById(R.id.imageView);


        //데이터 쓰기 권한요청
        ActivityCompat.requestPermissions(this, new String[]
                {android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, MODE_PRIVATE);


        btnPlay.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        pbMP3.setProgress(0);

        //seekbar 클릭시 해당위치에서 노래재생
        pbMP3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(getApplicationContext(),"보조시간"+ MusicActivity.mSeek,Toast.LENGTH_SHORT).show();
                // mediaPlayer.seekTo(MusicActivity.mSeek);
                mediaPlayer.start();
                mediaPlayer.seekTo(seekBar.getProgress());


            }
        });


        //자동으로 다음곡 넘어가는 기능
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (now + 1 < dataList.size()) {
                    flag = true;
                    check = true;
                    now++;
                    btnPlay.callOnClick();
                }
            }
        });

        //시작하면 일단 뮤직엑티비티에 갔다옴(static변수 메모리에 올리기 위해서)
        Intent intent = new Intent(MainActivity.this, MusicActivity.class);
        startActivity(intent);


        //이미지뷰와 그 옆에 누르면 뮤직엑티비티로 이동
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MusicActivity.class);
                startActivity(intent);
            }
        });

        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MusicActivity.class);
                startActivity(intent);
            }
        });

    }


    //리스트에 음악목록을 넣음
    private void getMusicList() {
        dataList.removeAll(dataList);
        //가져오고 싶은 컬럼 명을 나열. 음악의 아이디, 앰블럼 아이디, 제목, 아스티스트 정보
        String[] projection = {MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST};

        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection, MediaStore.Audio.Media.DATA + " like ? ",
                new String[]{"%mp3Projact%"}, null);

        while (cursor.moveToNext()) {
            MainData mainData = new MainData();
            mainData.setId(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
            mainData.setAlbumId(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
            mainData.setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
            mainData.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));

//            if(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)).equals("HYNN (박혜원)")){
//                dataList.add(mainData);
//            }
            dataList.add(mainData);

        }
        cursor.close();
    }


    @Override
    public void onClick(View v) {
        sqlDB = myHelper.getWritableDatabase();

        //모드변경을 위한 plus값 셋팅
        if (MusicActivity.abc == 1) {
            plus = 1;
        } else if (MusicActivity.abc == 2) {
            plus = (int) (Math.random() * dataList.size());
        } else if (MusicActivity.abc == 3) {
            plus = 0;
        }


        if(dataList.size()==0){
            Toast.makeText(getApplicationContext(),"좋아요 한 노래가 없습니다",Toast.LENGTH_SHORT).show();
            return;
        }


        switch (v.getId()) {
            case R.id.btnPlay:

                //처음부터 재생(다른거)
                if (check) {
                    flag = false;
                    check = false;
                    playbackPosition = 0;
                    btnPlay.setImageResource(R.mipmap.pause2);
                    MusicActivity.play.setImageResource(R.mipmap.pause2);
                    playMusic(dataList.get(now));
                    mediaPlayer.seekTo(playbackPosition);

                    //다시재생(같은거, 정지중)
                } else {
                    if (flag) {
                        flag = false;
                        btnPlay.setImageResource(R.mipmap.pause2);
                        MusicActivity.play.setImageResource(R.mipmap.pause2);
                        playMusic(dataList.get(now));
                        mediaPlayer.seekTo(playbackPosition);
                        //중지(같은거, 재생중)
                    } else {
                        flag = true;
                        btnPlay.setImageResource(R.mipmap.play2);
                        MusicActivity.play.setImageResource(R.mipmap.play2);
                        mediaPlayer.pause();
                        playbackPosition = mediaPlayer.getCurrentPosition();

                    }
                }

                break;

                //다음곡 재생
            case R.id.btnNext:
                playbackPosition = 0;
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                }
                flag = false; //재생중으로 바꿈


                if (now + plus > dataList.size() - 1) {
                    now = now + plus - (dataList.size());
                } else {
                    now = now + plus;
                }
                playMusic(dataList.get(now));
                MusicActivity.play.setImageResource(R.mipmap.pause2);

                break;

            case R.id.btnBack:
                playbackPosition = 0;
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                }
                flag = false;


                if (now - plus < 0) {
                    now = now - plus + (dataList.size());
                } else {
                    now = now - plus;
                }
                playMusic(dataList.get(now));
                MusicActivity.play.setImageResource(R.mipmap.pause2);

                break;
        }

    }

    public void playMusic(MainData mainData) {
        try {
            tvTitle.setSelected(true);
            tvSinger.setSelected(true);

            tvTitle.setText(mainData.getTitle());
            tvSinger.setText(mainData.getArtist());

            //데이터베이스에 좋아요 클릭한 항목이 있는지 체크
            cursor = sqlDB.rawQuery("SELECT * FROM musicTBL wHERE title ='" + mainData.getTitle() + "';", null);

            //없으면 하트에 불꺼짐
            if (cursor.getCount() == 0) {
                MusicActivity.like.setImageResource(R.mipmap.likef);
                MusicActivity.tvInit.setText("2");
                //있으면 불켜짐
            } else {
                MusicActivity.like.setImageResource(R.mipmap.liket);
                MusicActivity.tvInit.setText("1");


            }

            //뮤직액티비티에 재생중인 곡 목록 세팅
            MusicActivity.textView.setText(mainData.getTitle());
            MusicActivity.title.setText(mainData.getTitle());
            MusicActivity.singer.setText(mainData.getArtist());

            Bitmap bitmap = BitmapFactory.decodeFile(getCoverArtPath(Long.parseLong(mainData.getAlbumId()), getApplication()));
            imageView.setImageBitmap(bitmap);
            MusicActivity.album.setImageBitmap(bitmap);


            Uri musicURI = Uri.withAppendedPath(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "" + mainData.getId());
            mediaPlayer.reset();
            mediaPlayer.setDataSource(this, musicURI);
            mediaPlayer.prepare();
            mediaPlayer.start();

            //프로그래스바 쓰레드
            Thread thread = new Thread() {
                SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");

                @Override
                public void run() {
                    if (mediaPlayer == null) {
                        return;
                    }

                    //쓰레드 안에서는 위젯값을 바꾸면 안된다
                    //1.총 재생시간
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pbMP3.setMax(mediaPlayer.getDuration());
                            MusicActivity.seekBar.setMax(mediaPlayer.getDuration());
                            MusicActivity.sbEnd.setText(sdf.format(mediaPlayer.getDuration()));

                        }
                    });
                    while (mediaPlayer.isPlaying()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pbMP3.setProgress(mediaPlayer.getCurrentPosition());
                                MusicActivity.seekBar.setProgress(mediaPlayer.getCurrentPosition());
                                MusicActivity.sbStart.setText(sdf.format(mediaPlayer.getCurrentPosition()));

                            }
                        });

                        SystemClock.sleep(200);
                    }
                }
            };
            thread.start();

        } catch (Exception e) {
            Log.e("SimplePlayer", e.getMessage());
        }

    }


    public String getCoverArtPath(long albumId, Context context) {

        Cursor albumCursor = context.getContentResolver().query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Albums.ALBUM_ART},
                MediaStore.Audio.Albums._ID + " = ?",
                new String[]{Long.toString(albumId)},
                null
        );
        boolean queryResult = albumCursor.moveToFirst();
        String result = null;
        if (queryResult) {
            result = albumCursor.getString(0);
        }
        albumCursor.close();
        return result;
    }

    //상단바에 메뉴추가
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.like, menu);
        return true;
    }

    //상단바 메뉴 옵션
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            //전체목록 불러오기
            case R.id.likef:
                getMusicList();
                mainAdapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(), "전체리스트", Toast.LENGTH_SHORT).show();
                return true;

                //좋아요 한것만 불러오기
            case R.id.liket:
                int joayo = 0;
                dataList.removeAll(dataList);
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                sqlDB = myHelper.getWritableDatabase();


                //가져오고 싶은 컬럼 명을 나열. 음악의 아이디, 앰블럼 아이디, 제목, 아스티스트 정보
                String[] projection = {MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ALBUM_ID,
                        MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST};

                Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        projection, MediaStore.Audio.Media.DATA + " like ? ",
                        new String[]{"%mp3Projact%"}, null);

                while (cursor.moveToNext()) {
                    MainData mainData = new MainData();

                    mainData.setId(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
                    mainData.setAlbumId(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
                    mainData.setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                    mainData.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));

                    //곡 제목이 데이터베이스에 들어있냐?(좋아요 한거냐)
                    Cursor cursor1 = sqlDB.rawQuery("SELECT * FROM musicTBL WHERE title = '" + cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)) + "';", null);
                    if (cursor1.getCount() != 0) {
                        //있으면 리스트에 추가함
                        joayo = joayo + 1;
                        dataList.add(mainData);
                        cursor1.close();
                    }
                }
                Toast.makeText(getApplicationContext(), "좋아요 : " + joayo + "곡", Toast.LENGTH_SHORT).show();
                cursor.close();
                sqlDB.close();
                mainAdapter.notifyDataSetChanged();
                btnPlay.setImageResource(R.mipmap.play2);
                now = 0;
                flag = true;

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onBackPressed(){
        if(System.currentTimeMillis()-time>=2000){
            time=System.currentTimeMillis();
            Toast.makeText(getApplicationContext(),"뒤로가기 2번 = 종료.",Toast.LENGTH_SHORT).show();
        }else if(System.currentTimeMillis()-time<2000){
            mediaPlayer.stop();
            finish();
        }
    }

}
