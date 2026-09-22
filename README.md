# Rustech Technician App

Android app para sa mga technician mo. Kumokonekta sa parehong Firebase
project ng subscriber tracker dashboard mo (`installRequests` at
`repairRequests` collections).

## Paano i-setup (isang beses lang)

1. **Buksan sa Android Studio**
   - Android Studio → Open → piliin yung `RustechTechnician` folder (yung
     nasa loob nitong zip).
   - Hayaan mong mag-Gradle sync (may internet connection na kailangan).

2. **I-register ang app sa Firebase Console**
   - Buksan yung Firebase project mo (**rustech-subscriber-tracker**).
   - Project settings (gear icon) → General tab → i-scroll pababa sa
     "Your apps" → i-click yung Android icon (`</>` para sa Android) para
     magdagdag ng bagong app.
   - Package name: `com.rustech.technician` (dapat eksakto ito, kasi
     ito rin yung `applicationId` sa `app/build.gradle`).
   - I-download yung `google-services.json` na ibibigay nila.
   - Ilagay yung file na yun sa loob ng `app/` folder ng project
     (kasabay ng `build.gradle` sa loob ng `app/`).

3. **I-sync ulit at i-run**
   - Sa Android Studio, i-click yung "Sync Now" kung lumabas, tapos
     i-Run (▶️) papunta sa phone o emulator ng technician.

4. **Gumawa ng Firebase Auth account para sa bawat technician**
   - Firebase Console → Authentication → Users → Add user.
   - Email + password — ito yung gagamitin ng technician para mag-log in
     sa app. (Hindi na kailangan idagdag sa `ADMIN_EMAILS` — kahit sinong
     naka-login, makikita lang nila yung mga trabahong naka-assign sa
     kanilang email, base sa `firestore.rules`.)

5. **I-publish yung updated `firestore.rules`** (kung hindi mo pa)
   - Ito yung file na binigay ko na kasama nung "installRequests"
     changes — meron nang `isAssignedTech()` rule para dito.

## Paano gamitin

1. Sa **backlog dashboard mo (backlog.html)**, pag nag-**Schedule** ka ng
   install o repair, may field na "Technician email" — ilagay mo doon
   yung email ng technician na gagawa ng trabaho.
2. Yun na — lalabas na agad sa app ng technician (real-time, walang
   kailangang i-refresh).
3. Pag na-tap ng technician yung **"Tapos na" / "Naantala" / "Kanselado"**,
   agad na ma-a-update sa dashboard mo rin (dalawang direksyon, live sa
   parehong Firestore).

## Bakit walang password/API key na naka-encrypt

Yung `google-services.json` at Firebase config ay hindi confidential —
ang tunay na proteksyon ay yung `firestore.rules` (kung sino ang
pwedeng magbasa/mag-edit ng anong data). Kaya importante talagang
naka-publish yung latest rules bago mo ibigay ang app sa mga technician.
