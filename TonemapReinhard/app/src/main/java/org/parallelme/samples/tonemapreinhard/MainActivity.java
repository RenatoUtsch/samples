/**                                               _    __ ____
 *   _ __  ___ _____   ___   __  __   ___ __     / |  / /  __/
 *  |  _ \/ _ |  _  | / _ | / / / /  / __/ /    /  | / / /__
 *  |  __/ __ |  ___|/ __ |/ /_/ /__/ __/ /__  / / v  / /__
 *  |_| /_/ |_|_|\_\/_/ |_/____/___/___/____/ /_/  /_/____/
 *
 */

package org.parallelme.samples.tonemapreinhard;

import android.app.ProgressDialog;
import android.support.v8.renderscript.*;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Vector;

import org.parallelme.userlibrary.image.RGBE;

/**
 * Main activity of the tonemap application.
 *
 * @author Renato Utsch
 */
public class MainActivity extends Activity {
    private SeekBar mKeyValueSeekBar;
    private SeekBar mGammaCorrectionSeekBar;
    private Spinner mNumImagesSpinner;
    private Spinner mImageSpinner;
    private Spinner mRunWithSpinner;
    private TextView mKeyValueText;
    private TextView mGammaCorrectionText;
    private Bitmap mBitmap;
    private ImageView mDisplay;
    private ProgressDialog mProgressDialog;
    private TextView mBenchmarkText;
    private ReinhardJavaOperator mReinhardJavaOperator;
    private int mReinhardJavaOperatorID;
    private ReinhardUserLibraryOperator mReinhardUserLibraryOperator;
    private int mReinhardUserLibraryOperatorID;
    private ReinhardCompilerOperator mReinhardCompilerOperator;
    private int mReinhardCompilerOperatorID;
    private ReinhardCompilerRSOperator mReinhardCompilerRSOperator;
    private int mReinhardCompilerRSOperatorID;
    private ReinhardRenderScriptOperator mReinhardRenderScriptOperator;
    private int mReinhardRenderScriptOperatorID;
    private ReinhardOpenCLOperatorCPU mReinhardOpenCLOperatorCPU;
    private int mReinhardOpenCLOperatorCPUID;
    private ReinhardOpenCLOperatorGPU mReinhardOpenCLOperatorGPU;
    private int mReinhardOpenCLOperatorGPUID;
    private ReinhardScheduledOperator mReinhardScheduledOperator;
    private int mReinhardScheduledOperatorID;
    private int mNumImages;
    private int mImageResource;
    private float mKey;
    private float mGamma;

    private class ReinhardOperatorTask extends AsyncTask<ReinhardOperator, Void, Bitmap> {
        long mTime;

        @Override
        protected Bitmap doInBackground(ReinhardOperator... reinhard) {
            long time;

            // Select the resource, load it and create the bitmap.
            Vector<RGBE.ResourceData> resources = new Vector<>();
            Vector<Bitmap> bitmaps = new Vector<>();

            for(int i = 0; i < mNumImages; ++i) {
                resources.add(RGBE.loadFromResource(getResources(), mImageResource));
                bitmaps.add(Bitmap.createBitmap(resources.get(i).width, resources.get(i).height,
                        Bitmap.Config.ARGB_8888));
            }

            time = java.lang.System.currentTimeMillis();
            for(int i = 0; i < mNumImages; ++i)
                reinhard[0].runOp(resources.get(i), mKey, 1.0f / mGamma, bitmaps.get(i));
            reinhard[0].waitFinish();
            mTime = (java.lang.System.currentTimeMillis() - time);

            return bitmaps.lastElement();
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            // mBitmap and mBenchmarkText can only be modified in the UI thread.
            mBitmap = bitmap;
            mDisplay.setImageBitmap(mBitmap);

            String result = "Result: " + (mTime / mNumImages) + "ms";
            if(mNumImages > 1)
                result += " (mean)\nTotal: " + mTime + "ms";

            mBenchmarkText.setText(result);
            mProgressDialog.dismiss();
        }
    }

    private class NumImagesChangeListener implements Spinner.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            mNumImages = Integer.parseInt(mNumImagesSpinner.getItemAtPosition(pos).toString());
        }

        public void onNothingSelected(AdapterView<?> parent) { }
    }

    private class ImageChangeListener implements Spinner.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            switch(pos) {
                case 0: mImageResource = R.raw.bristolb; break;
                case 1: mImageResource = R.raw.clockbui; break;
                case 2: mImageResource = R.raw.crowfoot; break;
                case 3: mImageResource = R.raw.tahoe1; break;
                case 4: mImageResource = R.raw.tinterna; break;
                default:
                    throw new RuntimeException("Failed to select image resource");
            }
        }

        public void onNothingSelected(AdapterView<?> parent) { }
    }

    private class KeyValueSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mKey = ((float) progress) / 100.0f;
            mKeyValueText.setText(String.format(Locale.getDefault(), "%.2f", mKey));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) { }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) { }
    }

    private class GammaCorrectionSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mGamma = ((float) progress) / 10.0f;
            mGammaCorrectionText.setText(String.format(Locale.getDefault(), "%.1f", mGamma));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) { }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) { }
    }

    /** Called when the user clicks the Tonemap button */
    public void tonemap(View view) throws Exception {
        mProgressDialog.show();

        // Choose between the Reinhard Operator versions.
        if(mRunWithSpinner.getSelectedItemPosition() == mReinhardJavaOperatorID)
            new ReinhardOperatorTask().execute(mReinhardJavaOperator);
        else if(mRunWithSpinner.getSelectedItemPosition() == mReinhardUserLibraryOperatorID)
            new ReinhardOperatorTask().execute(mReinhardUserLibraryOperator);
        else if(mRunWithSpinner.getSelectedItemPosition() == mReinhardCompilerOperatorID)
            new ReinhardOperatorTask().execute(mReinhardCompilerOperator);
        else if(mRunWithSpinner.getSelectedItemPosition() == mReinhardCompilerRSOperatorID)
            new ReinhardOperatorTask().execute(mReinhardCompilerRSOperator);
        else if(mRunWithSpinner.getSelectedItemPosition() == mReinhardRenderScriptOperatorID)
            new ReinhardOperatorTask().execute(mReinhardRenderScriptOperator);
        else if(mRunWithSpinner.getSelectedItemPosition() == mReinhardOpenCLOperatorCPUID)
            new ReinhardOperatorTask().execute(mReinhardOpenCLOperatorCPU);
        else if(mRunWithSpinner.getSelectedItemPosition() == mReinhardOpenCLOperatorGPUID)
            new ReinhardOperatorTask().execute(mReinhardOpenCLOperatorGPU);
        else if(mRunWithSpinner.getSelectedItemPosition() == mReinhardScheduledOperatorID)
            new ReinhardOperatorTask().execute(mReinhardScheduledOperator);
        else
            throw new Exception("Invalid reinhard operator selected.");
    }

    /** Called when the user clicks the Reset button */
    public void reset(View view) {
        mImageSpinner.setSelection(0);
        mRunWithSpinner.setSelection(0);
        mNumImagesSpinner.setSelection(0);
        mKeyValueSeekBar.setProgress(18);
        mGammaCorrectionSeekBar.setProgress(16);
        mBenchmarkText.setText("");
        mDisplay.setImageResource(android.R.color.transparent);
        mBitmap = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RenderScript rs = RenderScript.create(this);
        mReinhardJavaOperator = new ReinhardJavaOperator();
        mReinhardCompilerOperator = new ReinhardCompilerOperator(rs);
        mReinhardCompilerRSOperator = new ReinhardCompilerRSOperator(rs);
        mReinhardRenderScriptOperator = new ReinhardRenderScriptOperator(rs);
        mReinhardOpenCLOperatorCPU = new ReinhardOpenCLOperatorCPU();
        mReinhardOpenCLOperatorGPU = new ReinhardOpenCLOperatorGPU();
        mReinhardUserLibraryOperator = new ReinhardUserLibraryOperator();
        mReinhardScheduledOperator = new ReinhardScheduledOperator();

        mImageSpinner = (Spinner) findViewById(R.id.spinner_image);
        ArrayAdapter<CharSequence> imageAdapter = ArrayAdapter.createFromResource(this,
                R.array.image_files, R.layout.spinner_layout);
        imageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mImageSpinner.setAdapter(imageAdapter);
        mImageSpinner.setOnItemSelectedListener(new ImageChangeListener());

        // RunWith options.
        int id = 0;
        ArrayList runWithOptions = new ArrayList();
        runWithOptions.add("Java");
        mReinhardJavaOperatorID = id++;
        runWithOptions.add("User Library");
        mReinhardUserLibraryOperatorID = id++;
        runWithOptions.add("Compiler");
        mReinhardCompilerOperatorID = id++;
        runWithOptions.add("Compiler Forced RS");
        mReinhardCompilerRSOperatorID = id++;
        if(mReinhardScheduledOperator.inited()) {
            runWithOptions.add("Hand Runtime");
            mReinhardScheduledOperatorID = id++;
        }
        runWithOptions.add("Hand RenderScript");
        mReinhardRenderScriptOperatorID = id++;
        if(mReinhardOpenCLOperatorCPU.inited()) {
            runWithOptions.add("Hand OpenCL CPU");
            mReinhardOpenCLOperatorCPUID = id++;
        }
        if(mReinhardOpenCLOperatorGPU.inited()) {
            runWithOptions.add("Hand OpenCL GPU");
            mReinhardOpenCLOperatorGPUID = id++;
        }

        mRunWithSpinner = (Spinner) findViewById(R.id.spinner_run_with);
        ArrayAdapter<CharSequence> runWithAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_layout, runWithOptions);
        runWithAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mRunWithSpinner.setAdapter(runWithAdapter);

        mNumImagesSpinner = (Spinner) findViewById(R.id.spinner_num_images);
        ArrayAdapter<CharSequence> numImagesAdapter = ArrayAdapter.createFromResource(this,
                R.array.run_count, R.layout.spinner_layout);
        numImagesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mNumImagesSpinner.setAdapter(numImagesAdapter);
        mNumImagesSpinner.setOnItemSelectedListener(new NumImagesChangeListener());

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading");
        mProgressDialog.setMessage("Applying tone mapping...");
        mProgressDialog.setCanceledOnTouchOutside(false);

        mKeyValueText = (TextView) findViewById(R.id.text_key_value);
        mKeyValueSeekBar = (SeekBar) findViewById(R.id.seekbar_key_value);
        mKeyValueSeekBar.setOnSeekBarChangeListener(new KeyValueSeekBarChangeListener());

        mGammaCorrectionText = (TextView) findViewById(R.id.text_gama_correction);
        mGammaCorrectionSeekBar = (SeekBar) findViewById(R.id.seekbar_gamma_correction);
        mGammaCorrectionSeekBar.setOnSeekBarChangeListener(new GammaCorrectionSeekBarChangeListener());

        mBenchmarkText = (TextView) findViewById(R.id.text_benchmark);
        mDisplay = (ImageView) findViewById(R.id.display);

        reset(mDisplay); // Random view, doesn't matter.
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
