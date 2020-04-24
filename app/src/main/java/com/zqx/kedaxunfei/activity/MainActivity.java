package com.zqx.kedaxunfei.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechSynthesizer;
import com.zqx.kedaxunfei.R;
import com.zqx.kedaxunfei.adapter.SpeakerAdapter;
import com.zqx.kedaxunfei.bean.Speaker;
import com.zqx.kedaxunfei.constants.Keys;
import com.zqx.kedaxunfei.utils.SpUtil;
import com.zqx.kedaxunfei.view.BottomDialog;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.et)
    EditText mEt;
    @BindView(R.id.iv_curspk_icon)
    ImageView mIvCurspkIcon;
    @BindView(R.id.tv_curspk_desc)
    TextView mTvCurspkDesc;
    @BindView(R.id.seekBar)
    SeekBar mSeekBar;

    private Dialog mDialog;
    private SpeakerAdapter mSpeakerAdapter;
    private SpeechSynthesizer mTts;
    private int curSpkPos;
    private List<Speaker> mSpeakers ;
    private Speaker mLastSpk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initSpeechParams();//初始化发音系统参数
        mSpeakers = Speaker.getAllSpeakers();//得到发音人模型的集合
        initLast();//初始化上一次退出时的EditText内容和上次选择的发音人数据
        initDialogLv();//初始化发音人选择对话框

    }

    private void initLast() {
        String lastText = SpUtil.getString(this, Keys.LAST_TEXT, "");
        mEt.setText(lastText);

        int lastPos = SpUtil.getInt(this, Keys.LAST_SPK_POS, 0);//默认为0位发音人
        mLastSpk = mSpeakers.get(lastPos);
        mIvCurspkIcon.setImageResource(mLastSpk.iconId);
        mTvCurspkDesc.setText(mLastSpk.desc);
        setVoice(mLastSpk.voice);
    }


    private void initSpeechParams() {
        mTts = SpeechSynthesizer.createSynthesizer(this, null);
        mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");//设置发音人
        mTts.setParameter(SpeechConstant.SPEED, "50");//设置语速
        mTts.setParameter(SpeechConstant.VOLUME, "80");//设置音量，范围0~100
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); //设置云端
    }

    private void setSpeed(int num) {
        mTts.setParameter(SpeechConstant.SPEED, "" + num);//设置语速
    }

    private void initDialogLv() {
        View viewDialog = View.inflate(this, R.layout.dialog_speaker, null);
        ListView lvSpeaker = (ListView) viewDialog.findViewById(R.id.lv_speaker);
        mSpeakerAdapter = new SpeakerAdapter(mSpeakers);
        lvSpeaker.setAdapter(mSpeakerAdapter);
        lvSpeaker.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Speaker item = mSpeakerAdapter.getItem(position);
                setVoice(item.voice);
                mTvCurspkDesc.setText(item.desc);
                mIvCurspkIcon.setImageResource(item.iconId);
                curSpkPos = position;
                mDialog.dismiss();
            }
        });

        mDialog = new BottomDialog(this);
        mDialog.setContentView(viewDialog);
        mDialog.setCanceledOnTouchOutside(true);

    }

    private void setVoice(String voice) {
        mTts.setParameter(SpeechConstant.VOICE_NAME, voice);
    }

    public void onSpeakClick(View view) {
        String text = mEt.getText().toString().trim();
        int progress = mSeekBar.getProgress();
        setSpeed(progress);
        mTts.startSpeaking(text, null);
    }

    public void onSpkerSwitchClick(View view) {
        mDialog.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SpUtil.saveInt(this, Keys.LAST_SPK_POS, curSpkPos);
        String lastText = mEt.getText().toString().trim();
        SpUtil.saveString(this, Keys.LAST_TEXT, lastText);
    }
}
