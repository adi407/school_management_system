#!/usr/bin/env python3
"""
══════════════════════════════════════════════════════════════════
  Gita Bal Niketan – CBSE School Data Seeder
  Faridabad, Sector 21D, Haryana 121001
══════════════════════════════════════════════════════════════════

  Seeds a complete school dataset via the School Manager REST API:
    ✦  1  Academic Year  (2025-26)
    ✦ 29  Subjects       (Primary → Senior Secondary)
    ✦ 36  Classes        (Grades 1-12, Sections A / B / C)
    ✦ 36  Teachers       (Primary / HOD / Junior / Stream)
    ✦     Class-teacher  assignments
    ✦     Subject→class→teacher assignments (~270 records)
    ✦  1,080 Students    (30 per section)  +  ~1,800 Guardians

  PRE-REQUISITES
    pip install requests
    Backend running on http://localhost:8080
    School Admin account for Gita Bal Niketan must exist.

  USAGE
    cd "School management System"
    python scripts/seed_school_data.py

  Edit BASE_URL / EMAIL / PASSWORD below if they differ.
══════════════════════════════════════════════════════════════════
"""

import json, random, sys, time
from datetime import date

try:
    import requests
except ImportError:
    print("ERROR: pip install requests  →  then re-run")
    sys.exit(1)

# ╔══════════════════════════════════════════════════════════╗
# ║  CONFIGURATION  — edit before running                    ║
# ╚══════════════════════════════════════════════════════════╝
BASE_URL     = "http://localhost:8081/api/v1"
EMAIL        = "admin@gitabn.edu.in"
PASSWORD     = "Admin@1234"
DELAY        = 0.06   # seconds between each API call (be gentle)
REPORT_FILE  = "seed_report_gitabn.json"

# ╔══════════════════════════════════════════════════════════╗
# ║  DATA DEFINITIONS                                        ║
# ╚══════════════════════════════════════════════════════════╝
random.seed(42)   # reproducible run

# ── Academic Year ──────────────────────────────────────────
AY = {"name":"2025-26","startDate":"2025-04-01","endDate":"2026-03-31","isCurrent":True}

# ── 29 Subjects ────────────────────────────────────────────
SUBJECTS = [
    # Primary (Grades 1-5)
    {"name":"English Language",       "code":"ENG-PRI","type":"CORE",    "creditHours":6},
    {"name":"Hindi",                  "code":"HIN-PRI","type":"CORE",    "creditHours":5},
    {"name":"Mathematics",            "code":"MTH-PRI","type":"CORE",    "creditHours":6},
    {"name":"Environmental Studies",  "code":"EVS",    "type":"CORE",    "creditHours":4},
    {"name":"General Knowledge",      "code":"GK",     "type":"ELECTIVE","creditHours":2},
    {"name":"Computer Applications",  "code":"CS-APP", "type":"ELECTIVE","creditHours":3},
    {"name":"Art & Craft",            "code":"ART",    "type":"ACTIVITY","creditHours":2},
    {"name":"Physical Education",     "code":"PHY-ED", "type":"ACTIVITY","creditHours":2},
    # Middle / Secondary additions (6-10)
    {"name":"Science",                "code":"SCI",    "type":"CORE",    "creditHours":6},
    {"name":"Social Science",         "code":"SST",    "type":"CORE",    "creditHours":5},
    {"name":"French",                 "code":"FRE",    "type":"LANGUAGE","creditHours":4},
    {"name":"Information Technology", "code":"IT",     "type":"ELECTIVE","creditHours":3},
    # Senior Secondary core (11-12)
    {"name":"English Core",           "code":"ENG-C",  "type":"CORE",    "creditHours":6},
    {"name":"Hindi Core",             "code":"HIN-C",  "type":"CORE",    "creditHours":5},
    {"name":"Physics",                "code":"PHY",    "type":"CORE",    "creditHours":7},
    {"name":"Chemistry",              "code":"CHE",    "type":"CORE",    "creditHours":7},
    {"name":"Mathematics (Higher)",   "code":"MTH-H",  "type":"CORE",    "creditHours":7},
    {"name":"Biology",                "code":"BIO",    "type":"CORE",    "creditHours":7},
    {"name":"Computer Science",       "code":"CS-ADV", "type":"CORE",    "creditHours":5},
    # Commerce stream
    {"name":"Accountancy",            "code":"ACC",    "type":"CORE",    "creditHours":6},
    {"name":"Business Studies",       "code":"BST",    "type":"CORE",    "creditHours":6},
    {"name":"Economics",              "code":"ECO",    "type":"CORE",    "creditHours":6},
    {"name":"Applied Mathematics",    "code":"MTH-A",  "type":"CORE",    "creditHours":6},
    {"name":"Informatics Practices",  "code":"IP",     "type":"ELECTIVE","creditHours":5},
    # Arts stream
    {"name":"History",                "code":"HIS",    "type":"CORE",    "creditHours":5},
    {"name":"Geography",              "code":"GEO",    "type":"CORE",    "creditHours":5},
    {"name":"Political Science",      "code":"POL",    "type":"CORE",    "creditHours":5},
    {"name":"Psychology",             "code":"PSY",    "type":"ELECTIVE","creditHours":5},
    {"name":"French (Senior)",        "code":"FRE-SR", "type":"ELECTIVE","creditHours":4},
]

# ── 36 Teachers ────────────────────────────────────────────
# Indices 0-7: Primary  |  8-15: HOD Senior  |  16-23: Junior  |  24-35: Stream (11-12)
STAFF = [
    # ── Primary Subject Teachers (Grades 1-5) ──
    {"firstName":"Sunita",   "lastName":"Sharma",    "email":"sunita.sharma@gitabn.edu.in",    "phone":"9812340001","dept":"Primary English",    "salary":30000},
    {"firstName":"Anita",    "lastName":"Verma",     "email":"anita.verma@gitabn.edu.in",      "phone":"9812340002","dept":"Primary Hindi",      "salary":28000},
    {"firstName":"Rekha",    "lastName":"Gupta",     "email":"rekha.gupta@gitabn.edu.in",      "phone":"9812340003","dept":"Primary Mathematics","salary":32000},
    {"firstName":"Kavita",   "lastName":"Singh",     "email":"kavita.singh@gitabn.edu.in",     "phone":"9812340004","dept":"Primary EVS",        "salary":27000},
    {"firstName":"Neha",     "lastName":"Kapoor",    "email":"neha.kapoor@gitabn.edu.in",      "phone":"9812340005","dept":"Primary GK",         "salary":25000},
    {"firstName":"Priya",    "lastName":"Bansal",    "email":"priya.bansal@gitabn.edu.in",     "phone":"9812340006","dept":"Primary Computer",   "salary":28000},
    {"firstName":"Meena",    "lastName":"Arora",     "email":"meena.arora@gitabn.edu.in",      "phone":"9812340007","dept":"Arts",               "salary":24000},
    {"firstName":"Ramesh",   "lastName":"Yadav",     "email":"ramesh.yadav@gitabn.edu.in",     "phone":"9812340008","dept":"Physical Education", "salary":26000},
    # ── HOD Senior Teachers (Grades 5-10, dept heads) ──
    {"firstName":"Amit",     "lastName":"Sharma",    "email":"amit.sharma@gitabn.edu.in",      "phone":"9812340009","dept":"English",            "salary":68000},
    {"firstName":"Sushma",   "lastName":"Mishra",    "email":"sushma.mishra@gitabn.edu.in",    "phone":"9812340010","dept":"Hindi",              "salary":58000},
    {"firstName":"Rohit",    "lastName":"Kumar",     "email":"rohit.kumar@gitabn.edu.in",      "phone":"9812340011","dept":"Mathematics",        "salary":65000},
    {"firstName":"Priti",    "lastName":"Agarwal",   "email":"priti.agarwal@gitabn.edu.in",    "phone":"9812340012","dept":"Science",            "salary":70000},
    {"firstName":"Sunita",   "lastName":"Chaudhary", "email":"sunita.chaudhary@gitabn.edu.in", "phone":"9812340013","dept":"Social Science",     "salary":58000},
    {"firstName":"Vikash",   "lastName":"Singh",     "email":"vikash.singh@gitabn.edu.in",     "phone":"9812340014","dept":"Computer Science",   "salary":55000},
    {"firstName":"Mahesh",   "lastName":"Tanwar",    "email":"mahesh.tanwar@gitabn.edu.in",    "phone":"9812340015","dept":"Physical Education", "salary":45000},
    {"firstName":"Priyanka", "lastName":"Mehta",     "email":"priyanka.mehta@gitabn.edu.in",   "phone":"9812340016","dept":"French",             "salary":52000},
    # ── Junior Subject Teachers (Grades 6-10) ──
    {"firstName":"Ravi",     "lastName":"Verma",     "email":"ravi.verma@gitabn.edu.in",       "phone":"9812340017","dept":"English",            "salary":38000},
    {"firstName":"Sangeeta", "lastName":"Sharma",    "email":"sangeeta.sharma@gitabn.edu.in",  "phone":"9812340018","dept":"Hindi",              "salary":35000},
    {"firstName":"Pankaj",   "lastName":"Gupta",     "email":"pankaj.gupta@gitabn.edu.in",     "phone":"9812340019","dept":"Mathematics",        "salary":40000},
    {"firstName":"Nidhi",    "lastName":"Jain",      "email":"nidhi.jain@gitabn.edu.in",       "phone":"9812340020","dept":"Science",            "salary":38000},
    {"firstName":"Alok",     "lastName":"Srivastava","email":"alok.srivastava@gitabn.edu.in",  "phone":"9812340021","dept":"Social Science",     "salary":36000},
    {"firstName":"Sumit",    "lastName":"Aggarwal",  "email":"sumit.aggarwal@gitabn.edu.in",   "phone":"9812340022","dept":"Computer Science",   "salary":38000},
    {"firstName":"Renu",     "lastName":"Yadav",     "email":"renu.yadav@gitabn.edu.in",       "phone":"9812340023","dept":"Physical Education", "salary":32000},
    {"firstName":"Priya",    "lastName":"Chopra",    "email":"priya.chopra@gitabn.edu.in",     "phone":"9812340024","dept":"French",             "salary":35000},
    # ── Senior Secondary Stream Teachers (Grades 11-12 only) ──
    {"firstName":"Deepak",   "lastName":"Agarwal",   "email":"deepak.agarwal@gitabn.edu.in",   "phone":"9812340025","dept":"Commerce",           "salary":55000},
    {"firstName":"Neha",     "lastName":"Goel",      "email":"neha.goel@gitabn.edu.in",        "phone":"9812340026","dept":"Commerce",           "salary":48000},
    {"firstName":"Sonia",    "lastName":"Bansal",    "email":"sonia.bansal@gitabn.edu.in",     "phone":"9812340027","dept":"Economics",          "salary":58000},
    {"firstName":"Rajesh",   "lastName":"Nanda",     "email":"rajesh.nanda@gitabn.edu.in",     "phone":"9812340028","dept":"Mathematics",        "salary":50000},
    {"firstName":"Anil",     "lastName":"Khanna",    "email":"anil.khanna@gitabn.edu.in",      "phone":"9812340029","dept":"Physics",            "salary":62000},
    {"firstName":"Smita",    "lastName":"Malhotra",  "email":"smita.malhotra@gitabn.edu.in",   "phone":"9812340030","dept":"Chemistry",          "salary":60000},
    {"firstName":"Ritu",     "lastName":"Jain",      "email":"ritu.jain@gitabn.edu.in",        "phone":"9812340031","dept":"Biology",            "salary":58000},
    {"firstName":"Vivek",    "lastName":"Khatri",    "email":"vivek.khatri@gitabn.edu.in",     "phone":"9812340032","dept":"Computer Science",   "salary":52000},
    {"firstName":"Anita",    "lastName":"Hooda",     "email":"anita.hooda@gitabn.edu.in",      "phone":"9812340033","dept":"History",            "salary":55000},
    {"firstName":"Suresh",   "lastName":"Mann",      "email":"suresh.mann@gitabn.edu.in",      "phone":"9812340034","dept":"Geography",          "salary":48000},
    {"firstName":"Pooja",    "lastName":"Saini",     "email":"pooja.saini@gitabn.edu.in",      "phone":"9812340035","dept":"Political Science",  "salary":45000},
    {"firstName":"Kavya",    "lastName":"Duhan",     "email":"kavya.duhan@gitabn.edu.in",      "phone":"9812340036","dept":"Arts",               "salary":50000},
]

# ── Class → class-teacher index (teacher idx in STAFF list) ──
# Key: "grade_section"  e.g. "6_B"
CLASS_TEACHER_IDX = {
    "1_A": 0, "1_B": 1, "1_C": 2,
    "2_A": 3, "2_B": 4, "2_C": 5,
    "3_A": 6, "3_B": 7, "3_C": 8,
    "4_A": 9, "4_B":10, "4_C":11,
    "5_A":12, "5_B":13, "5_C":14,
    "6_A":15, "6_B":16, "6_C":17,
    "7_A":18, "7_B":19, "7_C":20,
    "8_A":21, "8_B":22, "8_C":23,
    "9_A":24, "9_B":25, "9_C":26,
    "10_A":27,"10_B":28,"10_C":29,
    "11_A":30,"11_B":31,"11_C":32,
    "12_A":33,"12_B":34,"12_C":35,
}

# ── Subject group → list of (subj_code, teacher_idx) ──────
# Section A/C = HOD teaches; Section B = Junior teaches
# Grade 11-12: Section A=Commerce, B=PCM, C=PCB+Arts (mixed)
GRADE_SUBJECTS = {
    "primary": [                                   # Grades 1-5 (primary teachers idx 0-7)
        ("ENG-PRI",0),("HIN-PRI",1),("MTH-PRI",2),("EVS",3),
        ("GK",4),("CS-APP",5),("ART",6),("PHY-ED",7),
    ],
    "middle_hod": [                                # Grades 6-8, Sections A & C (HOD idx 8-15)
        ("ENG-PRI",8),("HIN-PRI",9),("MTH-PRI",10),("SCI",11),
        ("SST",12),("FRE",15),("CS-APP",13),("PHY-ED",14),
    ],
    "middle_jr": [                                 # Grades 6-8, Section B (Junior idx 16-23)
        ("ENG-PRI",16),("HIN-PRI",17),("MTH-PRI",18),("SCI",19),
        ("SST",20),("FRE",23),("CS-APP",21),("PHY-ED",22),
    ],
    "secondary_hod": [                             # Grades 9-10, Sections A & C
        ("ENG-PRI",8),("HIN-PRI",9),("MTH-PRI",10),("SCI",11),
        ("SST",12),("IT",13),("PHY-ED",14),
    ],
    "secondary_jr": [                              # Grades 9-10, Section B
        ("ENG-PRI",16),("HIN-PRI",17),("MTH-PRI",18),("SCI",19),
        ("SST",20),("IT",21),("PHY-ED",22),
    ],
    "sr_commerce": [                               # Gr 11-12 Section A
        ("ENG-C",8),("HIN-C",9),("ACC",24),("BST",25),
        ("ECO",26),("MTH-A",27),("IP",31),("FRE-SR",15),
    ],
    "sr_pcm": [                                    # Gr 11-12 Section B
        ("ENG-C",8),("HIN-C",9),("PHY",28),("CHE",29),
        ("MTH-H",10),("CS-ADV",31),("FRE-SR",15),
    ],
    "sr_pcb_arts": [                               # Gr 11-12 Section C (PCB + Arts, mixed)
        ("ENG-C",8),("HIN-C",9),
        ("PHY",28),("CHE",29),("BIO",30),("CS-ADV",31),  # PCB
        ("HIS",32),("GEO",33),("POL",34),("ECO",26),("PSY",35),  # Arts
        ("FRE-SR",15),                                             # Elective (both groups)
    ],
}

def get_subj_group(grade, section):
    if grade <= 5:               return "primary"
    if 6 <= grade <= 8:          return "middle_hod" if section in ("A","C") else "middle_jr"
    if 9 <= grade <= 10:         return "secondary_hod" if section in ("A","C") else "secondary_jr"
    if section == "A":           return "sr_commerce"
    if section == "B":           return "sr_pcm"
    return "sr_pcb_arts"

# ── Name & demographic pools ───────────────────────────────
MALE_F   = ["Arjun","Rahul","Aarav","Vivaan","Aditya","Kabir","Rohan","Dev",
             "Siddharth","Vikram","Nikhil","Ansh","Harsh","Yash","Dhruv","Akash",
             "Karan","Shiv","Rishi","Pranav","Ayush","Ishaan","Lakshya","Parth",
             "Manav","Tanmay","Chirag","Raghav","Kunal","Abhimanyu","Veer","Saksham",
             "Tushar","Mohit","Rishabh","Varun","Gaurav","Sumit","Shubham","Vikas",
             "Aman","Naveen","Hitesh","Kamal","Sunny","Deepak","Rajiv","Suresh","Vinay","Ashutosh"]
FEMALE_F = ["Priya","Sneha","Aarti","Diya","Ananya","Kavya","Riya","Siya","Pooja","Nisha",
             "Sakshi","Anjali","Muskan","Neha","Simran","Komal","Divya","Meera","Shruti","Tanvi",
             "Ishita","Aisha","Swati","Ritika","Garima","Palak","Sana","Preeti","Prachi","Saachi",
             "Avni","Manya","Vrinda","Aastha","Nandini","Deeksha","Shreya","Bhavna","Alka","Shalini",
             "Tanu","Varsha","Rekha","Sunita","Rajni","Geeta","Lalita","Savita","Usha","Madhuri"]
LAST_N   = ["Sharma","Gupta","Singh","Yadav","Kumar","Verma","Mishra","Agarwal","Bansal","Goel",
             "Jain","Khanna","Chopra","Malhotra","Bhatia","Sood","Arora","Nanda","Kapoor","Mehta",
             "Patel","Dahiya","Khatri","Chaudhary","Hooda","Tanwar","Deswal","Mann","Saini","Duhan"]

BLOOD_G  = ["A+","A-","B+","B-","AB+","AB-","O+","O-"]
BLOOD_W  = [30,1,20,1,8,1,37,2]
CATS     = ["GEN","OBC","SC","ST"]
CAT_W    = [65,20,12,3]
RELS     = ["Hindu","Sikh","Muslim","Christian","Jain"]
REL_W    = [75,14,7,2,2]
HOUSES   = ["Lotus","Rose","Jasmine","Marigold"]
OCC_M    = ["Government Employee","Business","Engineer","Doctor","Teacher",
             "Advocate","Accountant","Army Officer","Farmer","Software Engineer"]
OCC_F    = ["Housewife","Teacher","Doctor","Business","Government Employee","Nurse","Social Worker"]
OCC_SIB  = ["Student","Software Engineer","Teacher","CA Student","MBA Graduate","Engineer"]

# % male per class key "G_S"  (rest = female)
GENDER_RATIO_M = {
    "1_A":55,"1_B":50,"1_C":45, "2_A":50,"2_B":55,"2_C":50,
    "3_A":45,"3_B":50,"3_C":55, "4_A":55,"4_B":45,"4_C":50,
    "5_A":50,"5_B":55,"5_C":50, "6_A":55,"6_B":50,"6_C":45,
    "7_A":45,"7_B":55,"7_C":50, "8_A":50,"8_B":45,"8_C":55,
    "9_A":55,"9_B":50,"9_C":45, "10_A":50,"10_B":55,"10_C":50,
    # 11-12: Commerce more female, PCM more male, PCB+Arts mixed
    "11_A":40,"11_B":67,"11_C":50, "12_A":42,"12_B":63,"12_C":48,
}

# ╔══════════════════════════════════════════════════════════╗
# ║  REPORT STATE                                            ║
# ╚══════════════════════════════════════════════════════════╝
report = {
    "school":          "Gita Bal Niketan, Sector 21D, Faridabad 121001",
    "board":           "CBSE",
    "academic_year":   None,
    "subjects":        {"created":0,"failed":0,"ids":{}},
    "staff":           {"created":0,"failed":0,"salary_reference":[]},
    "classes":         {"created":0,"failed":0,"ids":{}},
    "class_subjects":  {"created":0,"failed":0},
    "students":        {"created":0,"failed":0,"by_class":{}},
    "guardians":       {"total":0},
    "errors":          [],
    "notes": {
        "grade_11_12_section_C": (
            "Section C in Grades 11-12 is a split section: first 15 students follow "
            "PCB/Medical stream, next 15 follow Arts stream. All subjects from both "
            "streams are assigned to this class for AI analysis purposes."
        ),
        "salary": (
            "Salary structures must be configured separately via Payroll → Salary Structures. "
            "See salary_reference in staff section for recommended figures."
        ),
        "teacher_password": "Teacher@2025 (set for all teacher accounts)",
    },
}

# ╔══════════════════════════════════════════════════════════╗
# ║  HTTP HELPERS                                            ║
# ╚══════════════════════════════════════════════════════════╝
_session = requests.Session()

def _headers(token=None):
    h = {"Content-Type": "application/json"}
    if token: h["Authorization"] = f"Bearer {token}"
    return h

def api_post(path, body, token=None):
    try:
        r = _session.post(f"{BASE_URL}{path}", json=body, headers=_headers(token), timeout=20)
        time.sleep(DELAY)
        return r
    except Exception as e:
        report["errors"].append(f"POST {path}: {e}")
        return None

def api_get(path, token):
    try:
        r = _session.get(f"{BASE_URL}{path}", headers=_headers(token), timeout=15)
        return r
    except Exception as e:
        return None

def log(msg, ok=True):
    sym = "✓" if ok else "✗"
    print(f"  {sym}  {msg}")

def step(msg):
    print(f"\n► {msg}")

# ╔══════════════════════════════════════════════════════════╗
# ║  SEED FUNCTIONS                                          ║
# ╚══════════════════════════════════════════════════════════╝

def do_login():
    step("Authentication")
    r = api_post("/auth/login", {"email": EMAIL, "password": PASSWORD})
    if not r or r.status_code != 200:
        print(f"\n✗  Login FAILED ({r.status_code if r else 'no response'})")
        print(f"   Check BASE_URL={BASE_URL} and credentials.")
        sys.exit(1)
    token = r.json()["accessToken"]
    log(f"Logged in as {EMAIL}")
    return token


def seed_academic_year(token):
    step("Academic Year")
    r = api_post("/academic-years", AY, token)
    if r and r.status_code in (200, 201):
        d = r.json()
        report["academic_year"] = {"id": d["id"], "name": d["name"]}
        log(f"Created: {d['name']}  (id: {d['id'][:8]}...)")
        return d["id"]

    # Already exists? Fetch the list
    r2 = api_get("/academic-years", token)
    if r2 and r2.status_code == 200:
        for y in r2.json():
            if y["name"] == AY["name"]:
                report["academic_year"] = {"id": y["id"], "name": y["name"]}
                log(f"Found existing: {y['name']}  (id: {y['id'][:8]}...)")
                return y["id"]

    log(f"Could not create or find academic year: {r.status_code if r else 'N/A'}", ok=False)
    report["errors"].append(f"Academic year: {r.text[:120] if r else 'timeout'}")
    return None


def seed_subjects(token):
    step(f"Subjects ({len(SUBJECTS)})")
    ids = {}
    for s in SUBJECTS:
        r = api_post("/subjects", {
            "name": s["name"], "code": s["code"],
            "type": s["type"], "creditHours": s["creditHours"],
        }, token)
        if r and r.status_code in (200, 201):
            d = r.json()
            ids[s["code"]] = d["id"]
            report["subjects"]["created"] += 1
            report["subjects"]["ids"][s["code"]] = d["id"]
        else:
            report["subjects"]["failed"] += 1
            err = f"Subject {s['code']}: {r.status_code if r else 'N/A'}"
            report["errors"].append(err)
            log(err, ok=False)

    log(f"{report['subjects']['created']}/{len(SUBJECTS)} subjects created"
        + (f"  ({report['subjects']['failed']} failed)" if report['subjects']['failed'] else ""))
    return ids


def seed_staff(token):
    step(f"Teaching Staff ({len(STAFF)})")
    ids = []
    for s in STAFF:
        r = api_post("/staff", {
            "email":      s["email"],
            "firstName":  s["firstName"],
            "lastName":   s["lastName"],
            "phone":      s["phone"],
            "department": s["dept"],
            "role":       "TEACHER",
            "password":   "Teacher@2025",
        }, token)
        if r and r.status_code in (200, 201):
            d = r.json()
            ids.append(d["id"])
            report["staff"]["created"] += 1
            report["staff"]["salary_reference"].append({
                "staffId":        d["id"],
                "name":           f"{s['firstName']} {s['lastName']}",
                "email":          s["email"],
                "department":     s["dept"],
                "monthlySalaryINR": s["salary"],
            })
        else:
            ids.append(None)   # keep index alignment
            report["staff"]["failed"] += 1
            err = f"Staff {s['email']}: {r.status_code if r else 'N/A'} {r.text[:60] if r else ''}"
            report["errors"].append(err)
            log(err, ok=False)

    log(f"{report['staff']['created']}/{len(STAFF)} teachers created"
        + (f"  ({report['staff']['failed']} failed)" if report['staff']['failed'] else ""))
    return ids


def seed_classes(token, staff_ids):
    step("Classes (36: Grades 1-12, Sections A / B / C)")
    cls_ids = {}
    stream = {"A": "Commerce", "B": "Non-Medical (PCM)", "C": "PCB & Arts"}

    for grade in range(1, 13):
        for sec in ["A", "B", "C"]:
            key = f"{grade}_{sec}"
            tidx = CLASS_TEACHER_IDX.get(key)
            tid  = staff_ids[tidx] if (tidx is not None and tidx < len(staff_ids)) else None

            name = (f"Grade {grade} — {stream[sec]}" if grade >= 11
                    else f"Grade {grade} — Section {sec}")

            body = {"grade": grade, "section": sec, "name": name, "capacity": 30}
            if tid:
                body["classTeacherId"] = tid

            r = api_post("/classes", body, token)
            if r and r.status_code in (200, 201):
                d = r.json()
                cls_ids[key] = d["id"]
                report["classes"]["created"] += 1
                report["classes"]["ids"][key] = d["id"]
            else:
                report["classes"]["failed"] += 1
                err = f"Class {key}: {r.status_code if r else 'N/A'}"
                report["errors"].append(err)
                log(err, ok=False)

    log(f"{report['classes']['created']}/36 classes created"
        + (f"  ({report['classes']['failed']} failed)" if report['classes']['failed'] else ""))
    return cls_ids


def assign_subjects(token, cls_ids, subj_ids, staff_ids):
    step("Assigning subjects → classes → teachers")
    for grade in range(1, 13):
        for sec in ["A", "B", "C"]:
            key      = f"{grade}_{sec}"
            class_id = cls_ids.get(key)
            if not class_id:
                continue
            group = get_subj_group(grade, sec)
            for code, tidx in GRADE_SUBJECTS[group]:
                sid = subj_ids.get(code)
                if not sid:
                    continue
                tid = staff_ids[tidx] if (tidx < len(staff_ids)) else None
                body = {"subjectId": sid}
                if tid:
                    body["teacherId"] = tid
                r = api_post(f"/classes/{class_id}/subjects", body, token)
                if r and r.status_code in (200, 201):
                    report["class_subjects"]["created"] += 1
                else:
                    report["class_subjects"]["failed"] += 1
                    if r and r.status_code != 409:
                        report["errors"].append(f"ClassSubj {key}/{code}: {r.status_code}")

    log(f"{report['class_subjects']['created']} subject assignments created"
        + (f"  ({report['class_subjects']['failed']} failed)" if report['class_subjects']['failed'] else ""))

# ╔══════════════════════════════════════════════════════════╗
# ║  STUDENT & GUARDIAN HELPERS                              ║
# ╚══════════════════════════════════════════════════════════╝

def rand_phone():
    return str(random.choice([7, 8, 9])) + str(random.randint(100000000, 999999999))

def rand_dob(grade):
    yr = (2025 - (grade + 5)) + random.randint(0, 1)
    return date(yr, random.randint(1, 12), random.randint(1, 28)).isoformat()

def admission_date(grade):
    return date(2025 - (grade - 1), 4, 1).isoformat()

def make_guardians(last_name, grade):
    gtype = random.choices(
        ["both", "mother", "father", "sibling"],
        weights=[60, 15, 10, 15]
    )[0]
    guardians = []
    addr = f"Sector {random.randint(1, 28)}, Faridabad, Haryana 121{random.randint(1,9):03d}"

    if gtype in ("both", "father"):
        fn  = random.choice(MALE_F) + " " + last_name
        guardians.append({
            "name": fn, "relation": "FATHER", "phone": rand_phone(),
            "email": fn.lower().replace(" ", ".") + "@gmail.com",
            "occupation": random.choice(OCC_M), "address": addr,
            "isPrimary": (gtype == "father"), "isAuthorizedPickup": True,
        })
    if gtype in ("both", "mother"):
        fn  = random.choice(FEMALE_F) + " " + last_name
        guardians.append({
            "name": fn, "relation": "MOTHER", "phone": rand_phone(),
            "email": None,
            "occupation": random.choice(OCC_F), "address": addr,
            "isPrimary": (gtype == "mother"), "isAuthorizedPickup": True,
        })
    if gtype == "sibling":
        is_bro = random.random() < 0.55
        fn = (random.choice(MALE_F) if is_bro else random.choice(FEMALE_F)) + " " + last_name
        guardians.append({
            "name": fn, "relation": "GUARDIAN", "phone": rand_phone(),
            "email": fn.lower().replace(" ", ".") + "@gmail.com",
            "occupation": random.choice(OCC_SIB), "address": addr,
            "isPrimary": True, "isAuthorizedPickup": True,
        })

    # Make sure exactly one is isPrimary=True
    if guardians and not any(g["isPrimary"] for g in guardians):
        guardians[0]["isPrimary"] = True
    return guardians


def seed_students(token, cls_ids, year_id):
    step("Students  (1,080 total — 30 per class)")
    print()

    for grade in range(1, 13):
        for sec in ["A", "B", "C"]:
            key      = f"{grade}_{sec}"
            class_id = cls_ids.get(key)
            if not class_id:
                log(f"Grade {grade}{sec}  — class ID missing, skip", ok=False)
                continue

            m_pct   = GENDER_RATIO_M.get(key, 50)
            m_count = round(30 * m_pct / 100)
            genders = ["MALE"] * m_count + ["FEMALE"] * (30 - m_count)
            random.shuffle(genders)

            ok_cnt = fail_cnt = 0
            for roll in range(1, 31):
                gender   = genders[roll - 1]
                fname    = random.choice(MALE_F if gender == "MALE" else FEMALE_F)
                lname    = random.choice(LAST_N)
                cat      = random.choices(CATS, weights=CAT_W)[0]
                religion = random.choices(RELS, weights=REL_W)[0]
                blood    = random.choices(BLOOD_G, weights=BLOOD_W)[0]
                guardian = make_guardians(lname, grade)

                body = {
                    "firstName":    fname,
                    "lastName":     lname,
                    "dateOfBirth":  rand_dob(grade),
                    "gender":       gender,
                    "admissionDate":admission_date(grade),
                    "classId":      class_id,
                    "rollNo":       str(roll),
                    "bloodGroup":   blood,
                    "nationality":  "Indian",
                    "religion":     religion,
                    "category":     cat,
                    "motherTongue": "Hindi",
                    "houseGroup":   random.choice(HOUSES),
                    "guardians":    guardian,
                }
                if year_id:
                    body["academicYearId"] = year_id

                r = api_post("/students", body, token)
                if r and r.status_code in (200, 201):
                    ok_cnt += 1
                    report["students"]["created"] += 1
                    report["guardians"]["total"]  += len(guardian)
                else:
                    fail_cnt += 1
                    report["students"]["failed"] += 1
                    if r and r.status_code not in (409,):
                        report["errors"].append(f"Student Gr{grade}{sec} roll{roll}: {r.status_code}")

            report["students"]["by_class"][key] = {"ok": ok_cnt, "fail": fail_cnt}
            sym = "✓" if fail_cnt == 0 else "⚠"
            print(f"    {sym}  Grade {grade:2d} {sec}  →  "
                  f"{ok_cnt}/30 enrolled  "
                  f"{'(' + str(fail_cnt) + ' failed)' if fail_cnt else ''}"
                  f"  [{m_count}M/{30-m_count}F]")

    log(f"\n   Total students : {report['students']['created']} enrolled, "
        f"{report['students']['failed']} failed")
    log(f"   Total guardians: {report['guardians']['total']} created")

# ╔══════════════════════════════════════════════════════════╗
# ║  FINAL REPORT                                            ║
# ╚══════════════════════════════════════════════════════════╝
def print_report():
    w = 62
    print("\n" + "═"*w)
    print("  SEED COMPLETE — GITA BAL NIKETAN CBSE SCHOOL")
    print("═"*w)
    ay   = report["academic_year"]
    s    = report["subjects"]
    st   = report["staff"]
    cl   = report["classes"]
    cs   = report["class_subjects"]
    stu  = report["students"]
    gua  = report["guardians"]
    errs = report["errors"]

    rows = [
        ("Academic Year",   f"{ay['name'] if ay else 'FAILED'}",             ""),
        ("Subjects",        f"{s['created']}/{len(SUBJECTS)}",                f"{s['failed']} failed" if s['failed'] else ""),
        ("Teachers",        f"{st['created']}/{len(STAFF)}",                  f"{st['failed']} failed" if st['failed'] else ""),
        ("Classes",         f"{cl['created']}/36",                            f"{cl['failed']} failed" if cl['failed'] else ""),
        ("Class-Subjects",  f"{cs['created']} assignments",                   f"{cs['failed']} failed" if cs['failed'] else ""),
        ("Students",        f"{stu['created']}/1080 enrolled",                f"{stu['failed']} failed" if stu['failed'] else ""),
        ("Guardians",       f"{gua['total']} created",                        ""),
    ]
    for label, val, note in rows:
        note_str = f"  ← {note}" if note else ""
        print(f"  {label:<18} {val:<28}{note_str}")

    print(f"\n  Errors : {len(errs)}")
    if errs:
        for e in errs[:8]:
            print(f"    ✗ {e}")
        if len(errs) > 8:
            print(f"    ... +{len(errs)-8} more (see {REPORT_FILE})")

    print(f"\n  SALARY REFERENCE  (set up in Payroll → Salary Structures)")
    salary_table = [
        ("Primary teachers (Gr 1–5)",         "₹24,000–₹32,000/mo"),
        ("Junior subject teachers (Gr 6–10)",  "₹32,000–₹40,000/mo"),
        ("HOD senior teachers",                "₹45,000–₹70,000/mo"),
        ("Sr. Secondary stream (Gr 11–12)",    "₹45,000–₹62,000/mo"),
    ]
    for group, range_ in salary_table:
        print(f"    {group:<42} {range_}")

    print(f"\n  Detailed report → {REPORT_FILE}")
    print("═"*w + "\n")

    with open(REPORT_FILE, "w", encoding="utf-8") as f:
        json.dump(report, f, indent=2, default=str)

# ╔══════════════════════════════════════════════════════════╗
# ║  MAIN                                                    ║
# ╚══════════════════════════════════════════════════════════╝
def main():
    print("\n" + "═"*62)
    print("  Gita Bal Niketan – CBSE School Data Seeder")
    print(f"  Target  : {BASE_URL}")
    print(f"  Account : {EMAIL}")
    print("═"*62)

    token    = do_login()
    year_id  = seed_academic_year(token)
    subj_ids = seed_subjects(token)
    staff_ids = seed_staff(token)
    cls_ids  = seed_classes(token, staff_ids)
    assign_subjects(token, cls_ids, subj_ids, staff_ids)
    seed_students(token, cls_ids, year_id)
    print_report()

if __name__ == "__main__":
    main()
