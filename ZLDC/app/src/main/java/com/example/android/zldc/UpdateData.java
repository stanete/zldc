package com.example.android.zldc;

import android.util.Log;

import java.util.ArrayList;

public class UpdateData {
    private Apk    apk;
    private Main   main;
    private Sub    sub;

    public Apk getApk() {
        if(apk == null)
            apk = new Apk();

        return apk;
    }

    public Main getMain() {
        if(main == null)
            main = new Main();

        return main;
    }
    public Sub getSub() {
        if(sub == null)
            sub = new Sub();

        return sub;
    }

    public void setApk(Apk apk) {
        this.apk = apk;
    }

    public void setMain(Main main) {
        this.main = main;
    }
    public void setSub(Sub sub) {
        this.sub = sub;
    }


    public static class Apk extends Baseclass{
        public Apk() {
            super();
        }

        public Apk(String filename, int main_ver, int sub_ver, int revision) {
            super(filename, main_ver, sub_ver, revision);
        }
    }

    public static class Main extends Baseclass{
        public Main() {
            super();
        }

        public Main(String filename, int main_ver, int sub_ver, int revision) {
            super(filename, main_ver, sub_ver, revision);
        }
    }

    public static class Sub extends Baseclass{
        public Sub() {
            super();
        }

        public Sub(String filename, int main_ver, int sub_ver, int revision) {
            super(filename, main_ver, sub_ver, revision);
        }
    }

    /**
     * Baseclass 除 Bat 之外的所有类的父类
     * */
    public static class Baseclass {
        private String filename;
        private String prover;
        private int    main_ver;
        private int    sub_ver;
        private int    revision;

        public Baseclass() {

        }

        public Baseclass(String filename, int main_ver, int sub_ver, int revision) {
            this.filename   = filename;
            this.main_ver   = main_ver;
            this.sub_ver    = sub_ver;
            this.revision    = revision;
        }

        public int getMain_ver() {
            return main_ver;
        }
        public int getSub_ver() {
            return sub_ver;
        }
        public String getFilename() {
            return filename;
        }
        public int getRevision() {
            return revision;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }
        public void setMain_ver(int main_ver) {
            this.main_ver = main_ver;
        }
        public void setSub_ver(int sub_ver) {
            this.sub_ver = sub_ver;
        }
        public void setRevision(int rev_ver) {
            this.revision = rev_ver;
        }

        @Override
        public String toString() {
            return "filename : "  + filename + "\t" +
                    "main_ver : " + main_ver + "\t" +
                    "sub_ver : "  + sub_ver + "\t" +
                    "revision : " + revision;
        }
    }

    @Override
    public String toString() {
        return "UpdateData{" +
                "apk=" + apk +
                ", main=" + main +
                ", sub=" + sub +
                '}';
    }
}
