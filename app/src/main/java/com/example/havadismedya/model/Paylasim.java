package com.example.havadismedya.model;

public class Paylasim {
    public final String email;
    public final String aciklama;
    public final String url;
    public final String id;
    public String begeniSayisi;
    public final String tarih;
    public final String paylasimTuru;
    public Paylasim(String id,String email, String aciklama, String url, String begeniSayisi,String tarih,String paylasimTuru) {
        this.id = id;
        this.email = email;
        this.aciklama = aciklama;
        this.url = url;
        this.begeniSayisi=begeniSayisi;
        this.tarih = tarih;
        this.paylasimTuru=paylasimTuru;
    }

}
