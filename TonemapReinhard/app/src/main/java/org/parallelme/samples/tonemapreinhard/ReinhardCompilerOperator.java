

package org.parallelme.samples.tonemapreinhard;

import android.graphics.Bitmap;

import org.parallelme.userlibrary.function.Foreach;
import org.parallelme.userlibrary.function.Reduce;
import org.parallelme.userlibrary.image.HDRImage;
import org.parallelme.userlibrary.image.Pixel;
import org.parallelme.userlibrary.image.RGBE;


import android.support.v8.renderscript.*;

public class ReinhardCompilerOperator implements ReinhardOperator {
	private ReinhardCompilerOperatorWrapper PM_parallelME;

	public ReinhardCompilerOperator(RenderScript PM_mRS) {
		this.PM_parallelME = new ReinhardCompilerOperatorWrapperImplPM();
		if (!this.PM_parallelME.isValid())
			this.PM_parallelME = new ReinhardCompilerOperatorWrapperImplRS(PM_mRS);
	}

    
    private float sum;
    private float max;
    private float scaleFactor;
    private float lmax2;

    public void runOp(RGBE.ResourceData resourceData, float key, float power, Bitmap bitmap) {
        PM_parallelME.inputBind1(resourceData.data, resourceData.width, resourceData.height);

        this.toYxy();
        this.logAverage(key);
        this.tonemap();
        this.toRgb();
        this.clamp(power);
        PM_parallelME.outputBind1(bitmap);
    }

    public void waitFinish() {
        
    }

    private void toYxy(){
        PM_parallelME.foreach1();
    }


    
    private void logAverage(float key) {
        sum = 0.0f;
        max = 0.0f;

        Pixel ret = PM_parallelME.reduce2();
        sum = ret.rgba.alpha;
        max = ret.rgba.red;

        
        float average = (float) Math.exp(sum / (float)(PM_parallelME.getHeight1() * PM_parallelME.getWidth2()));
        scaleFactor = key * (1.0f / average);

        
        lmax2 = (float) Math.pow(max * scaleFactor, 2);
    }

    private void tonemap() {
        final float fScaleFactor = scaleFactor;
        final float fLmax2 = lmax2;
        PM_parallelME.foreach3(fScaleFactor, fLmax2);
    }

    private void toRgb(){
        PM_parallelME.foreach4();
    }

    private void clamp(final float power) {
        PM_parallelME.foreach5(power);
    }
}