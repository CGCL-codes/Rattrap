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

import com.andymcsherry.library.R;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class Calculator extends AndyActivity {

	EditText input;
    TextView output;
    Button calculate;
    ClipboardManager clipboard;
    KeyboardView keyboard;
    static String[] matrices;
    
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.calculator);
		
		clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
		
		output = (TextView) findViewById(R.id.text);
        input = (EditText) findViewById(R.id.input);
        keyboard = (KeyboardView) findViewById(R.id.calcKeyboard);
        
        setUp(new EditText[]{input},keyboard);
		
        matrices = new String[6];
        SharedPreferences sp = getSharedPreferences("matrices", MODE_PRIVATE);
        for(int i = 0; i < 4; i++){
        	matrices[i] = sp.getString("m" + (i+1), null);
        }
        calculate = (Button) findViewById(R.id.calculate);
        calculate.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View v) {
        		String s = input.getText().toString();
        		try{
        			String out = CalcParser.calculate(getApplicationContext(), s).toString();
        			output.setText(out + "\n\n" + output.getText().toString());
        			input.setText("");
        		} catch(Exception e){
        			output.setText(e.getMessage() + "\n\n" + output.getText().toString());
        		}
        	}
        });
	}
	
	@Override
	protected void onEnter(){
		calculate.performClick();
	}
	
	@Override
	// Creates menu
	public boolean onCreateOptionsMenu(Menu menu) {
		if(CalcParser.answer != null && CalcParser.answer.type == AndyObject.MATRIX){
			menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.saveMatrix));
		}
		menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.matrices));
		menu.add(Menu.NONE, 99, Menu.NONE, getString(R.string.help));
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu){
		menu.clear();
		if(CalcParser.answer != null && CalcParser.answer.type == AndyObject.MATRIX){
			menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.saveMatrix));
		}
		menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.matrices));
		if(CalcParser.answer!=null){
			menu.add(Menu.NONE,2,Menu.NONE,getString(R.string.copy));
		}
		if(clipboard.hasText()){
			menu.add(Menu.NONE,3,Menu.NONE,getString(R.string.paste));
		}
		menu.add(Menu.NONE, 99, Menu.NONE, getString(R.string.help));
		return true;
	}
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId()==0){
			Intent intent = new Intent(getApplicationContext(), InsertFunction.class);
			intent.putExtra("motive", "matrix");
			intent.putExtra("matrix", CalcParser.answer.content);
			startActivity(intent);
			return true;
		}else if (item.getItemId() == 1) {
			startActivity(new Intent(getApplicationContext(),Matrices.class));
			return true;
		}else if (item.getItemId() == 2) {
			clipboard.setText(CalcParser.answer.content);
		}else if (item.getItemId() == 3) {
			input.setText(input.getText().toString()+clipboard.getText());
		} else if (item.getItemId() == 99) {
			startActivity(new Intent(getApplicationContext(),Help.class));
			return true;
		}else{
			Intent goToMarket = new Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=com.andymc.calculuspaid"));
			startActivity(goToMarket);
		}
		return false;
    }
    
    @Override
    public void type(String s){
    	if(currentEdit != null){
    		if(currentEdit.getText().toString().equals("")){
    			if(s.equals("/")||s.equals("+")||s.equals("*")||s.equals("-")||s.equals("^")){
    				s = "ans" + s;
    			}
    		}
    	}
    	super.type(s);
    }
}
