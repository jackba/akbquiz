package babybear.akbquiz;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class Welcome2 extends Activity {
	static final String TAG = "AKBquiz_Welcome";
	

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.warning);
        
        waring();
    }
    
    public void onStart(){
    	super.onStart();   	
    	
    	
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {  
        //在欢迎界面屏蔽BACK键  
        if(keyCode==KeyEvent.KEYCODE_BACK) {  
            return false;  
        }  
        return false;  
    }
    
    void waring(){
    	Animation anim = AnimationUtils.loadAnimation(this, R.anim.fade_in_out);
        anim.setFillEnabled(true); //启动Fill保持  
        anim.setFillAfter(true);  //设置动画的最后一帧是保持在View上面  

        findViewById(R.id.full).setAnimation(anim);
        
        new Handler().postDelayed(new Runnable() {
			public void run() {
				logo();
			}
		}, anim.getDuration());
        
        anim.startNow();
    }
    
    void logo(){
    	setContentView(R.layout.fanhuashe1);
    	
    	
    	
    	ImageView petal=(ImageView)findViewById(R.id.petal);
    	Animation anim1 = AnimationUtils.loadAnimation(this, R.anim.logo_petal);
        anim1.setFillEnabled(true); //启动Fill保持  
        anim1.setFillAfter(true);  //设置动画的最后一帧是保持在View上面  
        petal.setAnimation(anim1);
        anim1.startNow();
    	
        
        ImageView text=(ImageView)findViewById(R.id.text);
        Animation anim2 = AnimationUtils.loadAnimation(this, R.anim.logo_text);
        anim2.setFillEnabled(true); //启动Fill保持  
        anim2.setFillAfter(true);  //设置动画的最后一帧是保持在View上面  
        text.setAnimation(anim2);
        
        new Handler().postDelayed(new Runnable() {
			public void run() {
				logo_fadeout();
			}
		}, anim2.getDuration());
        anim2.startNow();
        
        ImageView smalltext=(ImageView)findViewById(R.id.smalltext);
        Animation anim3 = AnimationUtils.loadAnimation(this, R.anim.logo_smalltext);
        anim3.setFillEnabled(true); //启动Fill保持  
        anim3.setFillAfter(true);  //设置动画的最后一帧是保持在View上面  
        smalltext.setAnimation(anim3);
        anim3.startNow();
        
        
        
    }
    
    void logo_fadeout(){
    	ViewGroup layout = (ViewGroup) findViewById(R.id.full);
        Animation anim3 = AnimationUtils.loadAnimation(this, R.anim.wait_fade_out);
        anim3.setFillEnabled(true); //启动Fill保持  
        anim3.setFillAfter(true);  //设置动画的最后一帧是保持在View上面  
        new Handler().postDelayed(new Runnable() {
			public void run() {
				pregame();
			}
		}, anim3.getDuration());
        layout.setAnimation(anim3);
        anim3.startNow();
    }
    void pregame(){
    	setContentView(R.layout.activity_welcome);
    	ViewGroup layout = (ViewGroup)findViewById(R.id.full);
    	
    	
    	Animation anim = AnimationUtils.loadAnimation(this, R.anim.fade_in);
    	
    	anim.setFillEnabled(true);
    	anim.setFillAfter(true);
    	layout.setAnimation(anim);
    	anim.startNow();
    	
    	Animation anim1 = AnimationUtils.loadAnimation(this, R.anim.twinkling);
		TextView clickToStart = (TextView)findViewById(R.id.click_to_start);
    	clickToStart.setAnimation(anim1);
    	anim1.startNow();
    	
    	layout.setOnClickListener(new OnClickListener(){
    		
    		@Override
    		public void onClick(View v) {
    			twink();
    		}
    		
    	});
    }
    void twink(){
    	Animation anim2 = AnimationUtils.loadAnimation(this, R.anim.twinkling_quick);
		
		findViewById(R.id.click_to_start).setAnimation(anim2);
		
    	new Handler().postDelayed(new Runnable() {
 			public void run() {
 				Intent intent = new Intent(Welcome2.this, MainMenu.class);  
    			startActivity(intent);  
    			Welcome2.this.finish();
 			}
 		}, anim2.getDuration());
    	anim2.startNow();
    }
}
