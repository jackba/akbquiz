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
import android.widget.TextView;

public class Welcome extends Activity {
	//MultiAnimationManager multiAM = new MultiAnimationManager();
	ViewGroup layout =null;
	
	static final int PAGE_NUM=5;
	int counter = 0;
	int [] layoutIdList=new int [PAGE_NUM];
	
	
    
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.warning);
        layout = (ViewGroup)findViewById(R.id.full);
           
        layoutIdList[0]=R.layout.warning;
        layoutIdList[1]=R.layout.fanhuashe;
        layoutIdList[2]=R.layout.appreciacition;
        layoutIdList[3]=0;
        layoutIdList[4]=0;
        
        
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.fade_in_out);
        anim.setFillEnabled(true); //启动Fill保持  
        anim.setFillAfter(true);  //设置动画的最后一帧是保持在View上面  
        anim.setAnimationListener(al);
        
        layout.setAnimation(anim);
        counter++;
        
    }
    


    

    @Override  
    public boolean onKeyDown(int keyCode, KeyEvent event) {  
        //在欢迎界面屏蔽BACK键  
        if(keyCode==KeyEvent.KEYCODE_BACK) {  
            return false;  
        }  
        return false;  
    }
    
    AnimationListener al = new AnimationListener(){

		@Override
		public void onAnimationEnd(Animation arg0) {
			// TODO Auto-generated method stub
        	
        	
        	
            if(counter > PAGE_NUM || layoutIdList[counter]==0){
            	setContentView(R.layout.activity_welcome);
            	
            	layout = (ViewGroup)findViewById(R.id.full);
            	
            	Animation anim = AnimationUtils.loadAnimation(Welcome.this, R.anim.fade_in);
            	
            	anim.setFillEnabled(true);
            	anim.setFillAfter(true);
            	anim.setAnimationListener(new AnimationListener(){

					@Override
					public void onAnimationEnd(Animation animation) {
						// TODO Auto-generated method stub
						Animation anim = AnimationUtils.loadAnimation(Welcome.this, R.anim.twinkling);
						TextView clickToStart = (TextView)findViewById(R.id.click_to_start);
		            	clickToStart.setAnimation(anim);
		            	anim.startNow();
		            	
		            	layout.setOnClickListener(new OnClickListener(){
		            		
		            		@Override
		            		public void onClick(View v) {
		            			// TODO Auto-generated method stub
		            			
		            			Animation anim = AnimationUtils.loadAnimation(Welcome.this, R.anim.twinkling_quick);
		            			anim.setAnimationListener(new AnimationListener(){

									@Override
									public void onAnimationEnd(
											Animation animation) {
										// TODO Auto-generated method stub

				            			Intent intent = new Intent(Welcome.this, MainMenu.class);  
				            			startActivity(intent);  
				            			Welcome.this.finish();
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
		            			
				            	clickToStart.setAnimation(anim);
				            	anim.startNow();
		            			
		            		}
		            		
		            	});
		            	
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
            	
            	
            	layout.setAnimation(anim);
            	anim.startNow();
            					
            }else{
            	setContentView(layoutIdList[counter]);
            	layout = (ViewGroup)findViewById(R.id.full);
            	Animation anim = AnimationUtils.loadAnimation(Welcome.this, R.anim.fade_in_out);
                anim.setFillEnabled(true); //启动Fill保持  
                anim.setFillAfter(true);  //设置动画的最后一帧是保持在View上面  
            	anim.setAnimationListener(al);
            	layout.setAnimation(anim);
            }
        	
			
			counter++;
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onAnimationStart(Animation animation) {
			// TODO Auto-generated method stub
			
		}
	};
	
}
