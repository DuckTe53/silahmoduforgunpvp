# Silah Modu (Fabric, Minecraft 1.21.11)

8 ateşli silah, medkit ve 4 zırh (2 kask, 2 yelek) ekler.

## Kurulum

1. Minecraft 1.21.11 için Fabric Loader (0.19.5 önerilir) kur.
2. Fabric API (1.21.11) ve bu modun `.jar` dosyasını `mods` klasörüne at.

`.jar` dosyasını GitHub'dan almak için: repo > **Actions** > son çalışma > **Artifacts**.

## Kontroller

| Tuş | İşlev |
|---|---|
| Sol tık | Ateş et (otomatik silahlarda basılı tut) |
| **R** | Şarjör doldur |
| **B** | Silah menüsü (Creative'de "Silahlar" sekmesi de var) |
| Sağ tık (basılı tut) | Medkit kullan |

## Silahlar (1 kalp = 2 HP, oyuncu 20 HP)

| Silah | Mod | Şarjör / Yedek | Gövde | Kafa |
|---|---|---|---|---|
| Pistol | yarı-otomatik | 12 / 48 | 2,5 kalp | 7 kalp |
| Glock 17 | yarı-otomatik | 17 / 68 | 2 kalp | 5,5 kalp |
| M4A1-S (susturuculu) | otomatik | 20 / 80 | 2,5 kalp | 7 kalp |
| M4A4 | otomatik | 30 / 120 | 2,25 kalp | 6,5 kalp |
| AK-47 | otomatik | 30 / 120 | 3 kalp | 11 kalp (tek atışta öldürür) |
| AK-74M | otomatik | 30 / 120 | 2,5 kalp | 7,5 kalp |
| FN P90 | otomatik (~900/dk) | 50 / 150 | 1,5 kalp | 4 kalp |
| Kriss Vector | otomatik (~1200/dk) | 25 / 100 | 1,25 kalp | 3,5 kalp |

Silahların dayanıklılığı yoktur. Tüm değerler benim varsayımımdır; `src/main/java/com/silahmod/WeaponConfig.java`
içindeki `GunSpec` satırlarından değiştirilir (hasar, mermi, atış hızı, doldurma süresi, menzil, saçılma).

## Zırhlar ve medkit

- **Kask 1:** 2 zırh, silahların kafa hasarını %25 azaltır. **Kask 2:** 6 zırh + 2 sertlik + Koruma II + 2 altın kalp.
- **Yelek 1 / 2:** gövde hasarını %15 / %30 azaltır.
- **Medkit:** basılı tutunca bandaj sarılır; can dolar ve 60 sn boyunca 10 geçici kalp verir.

## Derleme (kendi bilgisayarında)

Derleme aracı için JDK 25 gerekir (oyun Java 21 hedefiyle derlenir).

```
./gradlew build
```

Çıktı: `build/libs/silah-modu-1.0.0.jar` (`-sources` olmayan dosya).

## Modelleri yeniden üretme

Silah modelleri `tools/gen_models.py`, zırhlar ve medkit `tools/gen_armor.py` ile üretilir (`python3 tools/gen_models.py`).
`tools/preview.py` modellerin önizlemesini çizer (matplotlib gerekir).
