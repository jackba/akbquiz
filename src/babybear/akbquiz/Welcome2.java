package babybear.akbquiz;


import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
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
        anim.setAnimationListener(new AnimationListener (){

    		@Override
    		public void onAnimationEnd(Animation animation) {
    			logo();
    		}

    		@Override
    		public void onAnimationRepeat(Animation animation) {
    			// TODO Auto-generated method stub
    		}

    		@Override
    		public void onAnimationStart(Animation animation) {
    			// TODO Auto-generated method stub
    		}
    		
        });
        ViewGroup layout = (ViewGroup) findViewById(R.id.full);
        layout.setAnimation(anim);
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
        anim2.setAnimationListener(new AnimationListener (){

    		@Override
    		public void onAnimationEnd(Animation animation) {
    			logo_fadeout();
    		}

    		@Override
    		public void onAnimationRepeat(Animation animation) {
    			// TODO Auto-generated method stub
    		}

    		@Override
    		public void onAnimationStart(Animation animation) {
    			// TODO Auto-generated method stub
    		}
    		
        });
        text.setAnimation(anim2);
        anim2.startNow();
        
        ImageView smalltext=(ImageView)findViewById(R.id.smalltext);
        Animation anim3 = AnimationUtils.loadAnimation(this, R.anim.logo_smalltext);
        anim3.setFillEnabled(true); //启动Fill保持  
        anim3.setFillAfter(true);  //设置动画的最后一帧是保持在View上面  
        anim3.setAnimationListener(new AnimationListener (){

    		@Override
    		public void onAnimationEnd(Animation animation) {
    			logo_fadeout();
    		}

    		@Override
    		public void onAnimationRepeat(Animation animation) {
    			// TODO Auto-generated method stub
    		}

    		@Override
    		public void onAnimationStart(Animation animation) {
    			// TODO Auto-generated method stub
    		}
    		
        });
        smalltext.setAnimation(anim3);
        anim3.startNow();
        
        
        
    }
    
    void logo_fadeout(){
    	ViewGroup layout = (ViewGroup) findViewById(R.id.full);
        Animation anim3 = AnimationUtils.loadAnimation(this, R.anim.wait_fade_out);
        anim3.setFillEnabled(true); //启动Fill保持  
        anim3.setFillAfter(true);  //设置动画的最后一帧是保持在View上面  
        anim3.setAnimationListener(new AnimationListener (){
    		@Override
    		public void onAnimationEnd(Animation animation) {
    			pregame();
    		}
    		@Override
    		public void onAnimationRepeat(Animation animation) {
    			// TODO Auto-generated method stub
    		}
    		@Override
    		public void onAnimationStart(Animation animation) {
    			// TODO Auto-generated method stub
    		}
        });
        layout.setAnimation(anim3);
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
    			// TODO Auto-generated method stub
    			
    			twink();
    			
    		}
    		
    	});
    }
    void twink(){
    	Animation anim2 = AnimationUtils.loadAnimation(this, R.anim.twinkling_quick);
		anim2.setAnimationListener(new AnimationListener(){

			@Override
			public void onAnimationEnd(
					Animation animation) {
				// TODO Auto-generated method stub

    			Intent intent = new Intent(Welcome2.this, MainMenu.class);  
    			startActivity(intent);  
    			Welcome2.this.finish();
			}

			@Override
			public void onAnimationRepeat(
					Animation animation) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onAnimationStart(
					Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
		});
		View clickToStart = (TextView)findViewById(R.id.click_to_start);
		
    	clickToStart.setAnimation(anim2);
    	anim2.startNow();
    }
}
