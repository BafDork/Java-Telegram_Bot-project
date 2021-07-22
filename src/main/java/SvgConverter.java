import java.io.*;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;


public class SvgConverter {

    public static void svgToPng(String imageURL) throws Exception {
        TranscoderInput input_svg_image = new TranscoderInput(imageURL);
        OutputStream png_ostream = new FileOutputStream(Bot.imagePath);
        TranscoderOutput output_png_image = new TranscoderOutput(png_ostream);
        PNGTranscoder my_converter = new PNGTranscoder();
        my_converter.transcode(input_svg_image, output_png_image);
        png_ostream.flush();
        png_ostream.close();

    }
}