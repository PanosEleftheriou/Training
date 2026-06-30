# Βοηθός Προπόνησης

Εργασία για το μάθημα Αντικειμενοστρεφής Προγραμματισμός 2  
Χαροκόπειο Πανεπιστήμιο – Τμήμα Πληροφορικής και Τηλεματικής

Εφαρμογή ανάλυσης αθλητικών δεδομένων από αρχεία TCX. Υποστηρίζει εκτέλεση από κονσόλα (Μέρος 1) και γραφική διεπαφή (Μέρος 2), καθώς και εκτίμηση VO2 Max (Προαιρετικό μέρος 2).

## Απαιτήσεις

- Java JDK 8+
- Maven 3.8+

## Μεταγλώττιση και εκτέλεση

```bash
mvn package
```

Κονσόλα:
```bash
java -jar target/coach.jar activity.tcx
```

Γραφική διεπαφή:
```bash
java -jar target/coach.jar
```

## Παράμετροι κονσόλας

| Παράμετρος | Περιγραφή |
|------------|-----------|
| `-w 70.5`  | Βάρος σε kg (ενεργοποιεί υπολογισμό θερμίδων) |
| `-a 28`    | Ηλικία |
| `-g male`  | Φύλο (male / female) |
| `-r 62`    | Παλμοί εν ηρεμία (για VO2 Max) |
| `-c 1/2/3` | Μέθοδος θερμίδων: 1=MET, 2=Καρδιακοί παλμοί, 3=VO2 Max |

Παράδειγμα:
```bash
java -jar target/coach.jar -w 70.5 -a 28 -g male -c 2 run.tcx
```

Έξοδος:
```
Activity: Running
Total Time: 45:32
Total Distance: 8.12 km
Avg Pace: 5.61 min/km
Avg Heart Rate: 152 bpm
Calories: 523 kcal
```

## Γραφική Διεπαφή

Η εφαρμογή έχει 4 καρτέλες:

- **Δραστηριότητες** – φόρτωση TCX αρχείων, εμφάνιση στατιστικών, επιλογή μεθόδου θερμίδων
- **Προσθήκη** – χειροκίνητη εισαγωγή δραστηριότητας
- **Προφίλ** – φύλο, ηλικία, βάρος, παλμοί εν ηρεμία, ημερήσιος στόχος θερμίδων
- **VO2 Max** – εκτίμηση αερόβιας ικανότητας με αξιολόγηση

## Δομή δεδομένων

Τα δεδομένα οργανώνονται ιεραρχικά:

```
Activity -> Lap -> Track -> Trackpoint
```

Τα στατιστικά υπολογίζονται από κάτω προς τα πάνω — κάθε επίπεδο αθροίζει τα επίπεδα κάτω του.

## Υπολογισμός θερμίδων

**Μέθοδος 1 – MET:**
```
C = μ * w * t
```

**Μέθοδος 2 – Καρδιακοί παλμοί (Keytel et al., 2005):**

Άνδρες: `C = (-55.0969 + 0.6309*h + 0.1966*w + 0.2017*a) * t / 4.184`

Γυναίκες: `C = (-20.4022 + 0.4472*h + 0.1263*w + 0.074*a) * t / 4.184`

**Μέθοδος 3 – VO2 Max:**
```
C = VO2Max * w * t / 200
```

## VO2 Max (Προαιρετικό μέρος 2)

Υπολογισμός με τη μέθοδο Uth et al. (2004):

```
MHR    = 220 - ηλικία
VO2Max = 15.3 * (MHR / RHR)
```

Το αποτέλεσμα αξιολογείται ως Άριστο / Καλό / Πάνω από Μέσο / Μέσο / Κάτω από Μέσο / Χαμηλό βάσει πινάκων αναφοράς ανά ηλικία και φύλο.

## Δομή κώδικα

```
src/main/java/
├── Main.java
├── model/
│   ├── Activity.java
│   ├── RunningActivity.java
│   ├── CyclingActivity.java
│   ├── WalkingActivity.java
│   ├── SwimmingActivity.java
│   ├── Lap.java
│   ├── Track.java
│   ├── Trackpoint.java
│   └── UserProfile.java
├── parser/
│   └── TcxParser.java
├── stats/
│   └── ActivityStats.java
├── calories/
│   ├── CalorieCalculator.java
│   ├── SimpleCalorieCalculator.java
│   ├── HRCalorieCalculator.java
│   └── VO2MaxCalorieCalculator.java
├── vo2max/
│   └── VO2MaxEstimator.java
└── gui/
    └── FitnessApp.java
```

## Αρχεία TCX για δοκιμή

https://github.com/firefly-cpp/tcx-test-files


