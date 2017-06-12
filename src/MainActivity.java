/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */

package com.testap;

import android.os.Bundle;
import android.widget.Toast;

import org.apache.cordova.*;
import com.plugins.bixolonprint.BXLService;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends CordovaActivity
{
    private static final String CONFIG_FILE_NAME = "jpos.xml";
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        loadUrl(launchUrl);
        createConfigFile();
        BXLService.setContext(this);
    }
    private void createConfigFile() {
        FileInputStream fis = null;

        try {
            fis = openFileInput(CONFIG_FILE_NAME);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            createNewConfigFile();
        }

        if (fis != null) {
            try {
                fis.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void createNewConfigFile() {
        InputStream is = getResources().openRawResource(R.raw.jpos);
        FileOutputStream fos = null;

        int available = 0;
        byte[] buffer = null;

        try {
            available = is.available();
            buffer = new byte[available];
            is.read(buffer);

            fos = openFileOutput(CONFIG_FILE_NAME, MODE_PRIVATE);
            fos.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}

