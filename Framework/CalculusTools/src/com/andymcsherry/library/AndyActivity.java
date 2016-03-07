/*
 * Copyright (C) 2010 Andrew P McSherry
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.andymcsherry.library;

import com.andymcsherry.library.actionbar.ActionBarActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.os.Bundle;

public abstract class AndyActivity extends ActionBarActivity {

	public EditText currentEdit;
	KeyboardView keyboard;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
	}
	
	
	/*
	 * Sets the current input of the KeyboardView
	 */
	protected void setInput(EditText cEdit){
		currentEdit = cEdit;
		//Suppresses android soft keyboard
		currentEdit.setInputType(0);
	}

	/*
	 * Adds appropriate listeners the EditText of an activity to
	 * set up interaction with KeyboardView and udpate validity.
	 */
	protected void setUp(EditText[] editArray, KeyboardView key){
		keyboard = key;
		for(EditText eT : editArray){
			eT.setOnFocusChangeListener(new OnFocusChangeListener(){
				@Override
				public void onFocusChange(View thisView, boolean focused) {
					if(focused){
						setInput((EditText)thisView);
						if(keyboard != null){
							keyboard.setVisibility(View.VISIBLE);
						}
					}				
				}
			});
			eT.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View thisView) {
					setInput((EditText)thisView);
					keyboard.setVisibility(View.VISIBLE);				
				}
			});
			eT.addTextChangedListener(new TextWatcher() {

				@Override
				public void afterTextChanged(Editable arg0) {
					updateValidity();		
				}
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {}
				@Override
				public void onTextChanged(CharSequence s, int start, int before,
						int count) {}

			});
		}
	}

	/*
	 * Called by KeyboardView when there is user input
	 */
	public void type(String s){
		if(currentEdit != null){
			int cur = currentEdit.getSelectionStart();
			if(s.equals("del")||s.equals("del(")){
				if(cur != 0){
					currentEdit.getText().delete(cur-1, cur);
				}			
			}else if(s.equals("clear")){
				currentEdit.setText("");
			}else if(s.equals("ENTER")){
				onEnter();
			}else{
				currentEdit.getText().insert(cur, s);
			}
		}
	}
	
	/*
	 * Should be overridden by sub-classes to handle the user pressing
	 * enter.
	 */
	protected void onEnter(){}
	
	/*
	 * Should be override by subclasses with an appropriate
	 * way to check the validity of inputs.
	 */
	protected void updateValidity(){}
	
	@Override public boolean onKeyDown(int keyCode, KeyEvent event){
		if(keyCode==KeyEvent.KEYCODE_BACK && keyboard.getVisibility()==View.VISIBLE){
			keyboard.setVisibility(View.GONE);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event){
		updateValidity();
		if(keyCode==KeyEvent.KEYCODE_ENTER){
			onEnter();
		}
		return super.onKeyUp(keyCode, event);
	}
}
