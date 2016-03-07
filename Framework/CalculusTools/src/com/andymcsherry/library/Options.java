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

import android.app.Activity;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;

public class Options extends Activity {

	SharedPreferences sp;
	CheckBox haptic, graph, calculator, derivative, integral, formula;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.options);
		sp = getSharedPreferences("options", MODE_PRIVATE);
		haptic = (CheckBox) findViewById(R.id.hapticCheck);
		graph = (CheckBox) findViewById(R.id.graphCheck);
		calculator = (CheckBox) findViewById(R.id.calculatorCheck);
		derivative = (CheckBox) findViewById(R.id.derivativeCheck);
		integral = (CheckBox) findViewById(R.id.integralCheck);
		formula = (CheckBox) findViewById(R.id.formulaCheck);
		haptic.setChecked(sp.getBoolean("haptic", true));
		graph.setChecked(sp.getBoolean("graphLauncher",false));
		calculator.setChecked(sp.getBoolean("calculatorLauncher",false));
		derivative.setChecked(sp.getBoolean("derivativeLauncher",false));
		integral.setChecked(sp.getBoolean("integralLauncher",false));
		formula.setChecked(sp.getBoolean("formulaLauncher",false));
		
		haptic.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				SharedPreferences.Editor edit = sp.edit();
				if(haptic.isChecked()){
					edit.putBoolean("haptic", true);
				}else{
					edit.putBoolean("haptic", false);
				}
				edit.commit();
			}
			
		});
		
		graph.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				PackageManager manager = getPackageManager();
				
				SharedPreferences.Editor edit = sp.edit();
				if(graph.isChecked()){
					edit.putBoolean("graphLauncher", true);
					manager.setComponentEnabledSetting(new ComponentName(Options.this,"com.andymc.derivativelibrary.GraphLauncher"), 
							PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
				}else{
					edit.putBoolean("graphLauncher", false);
					manager.setComponentEnabledSetting(new ComponentName(Options.this,"com.andymc.derivativelibrary.GraphLauncher"), 
							PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
				}
				edit.commit();
			}
			
		});
		
		calculator.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				PackageManager manager = getPackageManager();
				
				SharedPreferences.Editor edit = sp.edit();
				if(graph.isChecked()){
					edit.putBoolean("calculatorLauncher", true);
					manager.setComponentEnabledSetting(new ComponentName(Options.this,"com.andymc.derivativelibrary.CalculatorLauncher"), 
							PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
				}else{
					edit.putBoolean("calculatorLauncher", false);
					manager.setComponentEnabledSetting(new ComponentName(Options.this,"com.andymc.derivativelibrary.CalculatorLauncher"), 
							PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
				}
				edit.commit();
			}
			
		});
		
		derivative.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				PackageManager manager = getPackageManager();
				
				SharedPreferences.Editor edit = sp.edit();
				if(graph.isChecked()){
					edit.putBoolean("derivativeLauncher", true);
					manager.setComponentEnabledSetting(new ComponentName(Options.this,"com.andymc.derivativelibrary.DerivativeLauncher"), 
							PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
				}else{
					edit.putBoolean("derivativeLauncher", false);
					manager.setComponentEnabledSetting(new ComponentName(Options.this,"com.andymc.derivativelibrary.DerivativeLauncher"), 
							PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
				}
				edit.commit();
			}
			
		});
		
		integral.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				PackageManager manager = getPackageManager();
				
				SharedPreferences.Editor edit = sp.edit();
				if(graph.isChecked()){
					edit.putBoolean("integralLauncher", true);
					manager.setComponentEnabledSetting(new ComponentName(Options.this,"com.andymc.derivativelibrary.IntegralLauncher"), 
							PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
				}else{
					edit.putBoolean("integralLauncher", false);
					manager.setComponentEnabledSetting(new ComponentName(Options.this,"com.andymc.derivativelibrary.IntegralLauncher"), 
							PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
				}
				edit.commit();
			}
			
		});
		
		formula.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				PackageManager manager = getPackageManager();
				
				SharedPreferences.Editor edit = sp.edit();
				if(graph.isChecked()){
					edit.putBoolean("formulaLauncher", true);
					manager.setComponentEnabledSetting(new ComponentName(Options.this,"com.andymc.derivativelibrary.FormulaLauncher"), 
							PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
				}else{
					edit.putBoolean("formulaLauncher", false);
					manager.setComponentEnabledSetting(new ComponentName(Options.this,"com.andymc.derivativelibrary.FormulaLauncher"), 
							PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
				}
				edit.commit();
			}
			
		});
	}
}
