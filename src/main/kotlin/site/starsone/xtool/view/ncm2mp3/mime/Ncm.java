package site.starsone.xtool.view.ncm2mp3.mime;

/**
 * @author charlottexiao
 */

public class Ncm {

    /**
     * NCM的文件路径
     */
    private String ncmFile;

    /**
     * 转换后文件路径
     */
    private String outFile;

    /**
     * 头信息
     */
    private Mata mata;

    /**
     * 封面信息
     */
    private byte[] image;

    public String getNcmFile() {
        return ncmFile;
    }

    public Ncm setNcmFile(String ncmFile) {
        this.ncmFile = ncmFile;
        return this;
    }

    public String getOutFile() {
        return outFile;
    }

    public Ncm setOutFile(String outFile) {
        this.outFile = outFile;
        return this;
    }

    public Mata getMata() {
        return mata;
    }

    public Ncm setMata(Mata mata) {
        this.mata = mata;
        return this;
    }

    public byte[] getImage() {
        return image;
    }

    public Ncm setImage(byte[] image) {
        this.image = image;
        return this;
    }
}
