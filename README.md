# 오늘의 가게

소규모 매장의 재고, 공지사항, 체크리스트, 반복 업무를 한 앱에서 관리할 수 있도록 만든 Android 매장 운영 관리 앱입니다.

현재 Google Play Store에 등록된 앱이며, 본 저장소는 포트폴리오 공개를 위한 Android 클라이언트 코드입니다.

- Play Store: https://play.google.com/store/apps/details?id=com.hyeiin.stock
- Package: `com.hyeiin.stock`
- Platform: Android
- Language: Java

---

## 프로젝트 개요

소규모 매장에서는 재고 현황, 전달사항, 반복 업무, 직원별 업무 완료 여부가 여러 채널에 흩어져 관리되는 경우가 많습니다.  
이로 인해 재고 누락, 업무 전달 누락, 반복 업무 체크 실패와 같은 문제가 발생할 수 있습니다.

**오늘의 가게**는 사장과 직원이 하나의 매장 공간에서 재고, 공지사항, 특이사항, 체크리스트, 루틴 업무를 함께 관리할 수 있도록 만든 Android 앱입니다.

Firebase Authentication을 통해 사용자를 인증하고, Cloud Firestore를 활용해 매장별 데이터를 실시간으로 관리합니다.  
Cloud Functions는 매장 생성, 초대코드 발급, 직원 참여, 매장 삭제처럼 서버 권한 검증이 필요한 작업에 사용했습니다.

운영 환경의 Firebase 설정, Cloud Functions 코드, Firestore 보안 규칙 전문, 배포 설정, 브랜드 이미지 리소스는 실제 서비스 보호를 위해 public 저장소에 포함하지 않습니다.

---

## 주요 기능

### 회원가입 및 로그인

- 이메일/비밀번호 회원가입 및 로그인
- 이메일 인증 확인
- 사장/직원 역할 구분
- 비밀번호 재설정

| 클래스 | 설명 |
|---|---|
| `LoginActivity` | 로그인, 이메일 인증 확인, 비밀번호 재설정을 처리 |
| `RegisterActivity` | 사장/직원 회원가입, 약관 동의, 이메일 인증 발송을 처리 |
| `FirebaseManager` | Firebase Auth, Firestore, Functions 접근 지점을 관리 |

### 매장 생성 및 초대코드 참여

- 사장 계정의 매장 생성
- 직원의 초대코드 기반 매장 참여
- 사용자가 참여 중인 매장 목록 조회
- 매장 선택 후 홈 화면 진입

| 클래스 | 설명 |
|---|---|
| `StoreSelectActivity` | 매장 목록 조회, 매장 입장, 매장 생성, 초대코드 참여를 처리 |
| `MainActivity` | 선택된 매장의 홈, 재고, 공지사항, 체크리스트 화면 전환을 관리 |
| `UserSession` | 로그인 사용자와 현재 매장 정보를 앱 내부에서 관리 |

### 재고 관리

- 재고 등록, 수정, 삭제
- 카테고리별 필터링
- 재고명 검색
- 수량 및 단위 관리
- 재고 이미지 업로드 및 표시

| 클래스 | 설명 |
|---|---|
| `InventoryFragment` | 재고 목록 조회, 검색, 카테고리 필터링, 저장 및 삭제를 처리 |
| `InventoryBottomSheet` | 재고 등록/수정 입력 화면과 이미지 업로드를 처리 |
| `InventoryAdapter` | 재고 목록을 RecyclerView에 표시 |
| `InventoryItem` | 재고 데이터 모델입니다. |

### 공지사항 및 특이사항 관리

- 일반 공지사항 작성 및 조회
- 특이사항 작성 및 조회
- 공지 유형별 탭 필터링
- 작성자 및 권한에 따른 수정/삭제 제어

| 클래스 | 설명 |
|---|---|
| `AnnouncementFragment` | 공지사항 목록 조회, 탭 필터링, 삭제를 처리 |
| `AnnouncementWriteActivity` | 공지사항 및 특이사항 작성을 처리 |
| `AnnouncementDetailActivity` | 공지사항 상세 화면을 표시 |

### 체크리스트 관리

- 공용 체크리스트와 개인 체크리스트 분리
- 체크 완료자와 완료 시간 기록
- 날짜별 완료 기록 조회
- 체크리스트 진행률 표시

| 클래스 | 설명 |
|---|---|
| `ChecklistFragment` | 공용/개인 체크리스트 조회, 추가, 루틴 반영, 진행률 계산을 처리 |
| `ChecklistAdapter` | 체크리스트 항목 표시, 완료 상태 변경, 삭제를 처리 |
| `ChecklistItem` | 체크리스트 데이터 모델 |
| `HistoryBottomSheet` | 날짜별 체크리스트 완료 기록을 조회 |

### 루틴 관리

- 반복 업무 루틴 생성
- 루틴 세부 항목 추가 및 삭제
- 루틴을 체크리스트에 반영
- 사용자별 루틴 관리

| 클래스 | 설명 |
|---|---|
| `RoutineManageActivity` | 루틴 생성, 수정, 삭제, 세부 항목 관리를 처리 |
| `Routine` | 루틴 데이터 모델 |
| `RoutineListAdapter` | 체크리스트에 반영할 루틴 목록을 표시 |

---

## 서비스 흐름

```text
회원가입 / 로그인
        ↓
이메일 인증
        ↓
사장: 매장 생성
직원: 초대코드로 매장 참여
        ↓
매장 선택
        ↓
홈 화면 진입
        ↓
재고 / 공지사항 / 체크리스트 / 루틴 관리
        ↓
Firestore 실시간 동기화
        ↓
Firestore Rules와 App Check를 통한 접근 제어
```

---

## 개발 환경

| 항목 | 내용 |
|---|---|
| Language | Java |
| Platform | Android |
| minSdk | 33 |
| targetSdk | 36 |
| Backend | Firebase |
| Authentication | Firebase Authentication |
| Database | Cloud Firestore |
| Server Logic | Cloud Functions |
| Storage | Firebase Storage |
| Security | Firebase App Check, Firestore Security Rules |
| Image Loading | Glide |
| UI | Material Components |

---

## 프로젝트 구조

```text
TodayStore/
├── app/
│   ├── build.gradle
│   └── src/
│       ├── debug/
│       ├── release/
│       └── main/
│           ├── AndroidManifest.xml
│           ├── java/com/hyeiin/stock/
│           └── res/
├── gradle/
├── build.gradle
├── settings.gradle
└── README.md
```

| 파일/폴더 | 설명 |
|---|---|
| `app/src/main/java/com/hyeiin/stock` | Android 앱 주요 Activity, Fragment, Adapter, Model 코드 |
| `app/src/debug` | Debug 빌드용 App Check Provider 설정 |
| `app/src/release` | Release 빌드용 App Check Provider 설정 |
| `app/src/main/res` | 앱 화면, 공통 UI 아이콘, 폰트, 레이아웃 리소스 |

---

## 실행 안내

본 저장소에는 운영 Firebase 설정, 서버 로직, 실제 앱 로고와 런처 아이콘이 포함되어 있지 않습니다.  
따라서 저장소를 클론한 직후 실제 운영 앱과 동일하게 실행되지는 않습니다.

로컬에서 실행하려면 별도의 Firebase 프로젝트를 만들고 다음 설정을 직접 구성해야 합니다.

- Firebase Authentication: Email/Password
- Cloud Firestore
- Firebase Storage
- Cloud Functions
- Firebase App Check
- `app/google-services.json`

운영 환경의 Firebase 프로젝트 정보, 배포 키, Cloud Functions 코드, Firestore 보안 규칙은 public 저장소에 공개하지 않습니다.

---

## 공개 저장소 관리

다음 항목은 public 저장소에 포함하지 않습니다.

- `app/google-services.json`
- `local.properties`
- `.firebaserc`
- `firebase.json`
- `firestore.rules`
- `firestore.indexes.json`
- `functions/`
- 실제 운영 앱 로고 및 런처 아이콘
- `.env*`
- Firebase service account JSON
- keystore 및 signing 설정
- APK/AAB 파일
- 빌드 산출물

본 저장소는 기존 개발 이력을 그대로 공개하지 않고,  
민감 파일이 포함된 과거 커밋을 제외하기 위해 clean import 방식으로 구성했습니다.

---

## Release Notes

### v1.0.1

- Firebase App Check 적용
- Debug/Release App Check Provider 분리
- 매장 초대코드 발급 및 참여 기능 개선
- 루틴 권한 및 Firestore Security Rules 개선
- 재고 이미지 업로드 기능 추가
- Store 삭제 시 관련 데이터 정리 로직 추가

### v1.0.0

- 이메일 회원가입 및 로그인 구현
- 사장/직원 역할 구분 구현
- 매장 생성 및 선택 기능 구현
- 재고 관리 기능 구현
- 공지사항 및 특이사항 기능 구현
- 공용/개인 체크리스트 기능 구현
- 반복 업무 루틴 관리 기능 구현

### 개선 예정

- 테스트 코드 보강
- 알림 기능 추가
- 관리자 문의 기능 개선
- 다중 매장 전환 UX 개선
- 운영 통계 화면 추가

---

## License

본 저장소는 포트폴리오 및 학습 참고 목적으로 공개되어 있습니다.  
저장소의 코드를 그대로 사용하거나 수정하여 발생하는 문제, 손해, 서비스 장애, 보안 문제에 대해 작성자는 책임지지 않습니다.  
운영 환경의 Firebase 설정, Cloud Functions 코드, Firestore 보안 규칙, 배포 키 등은 public 저장소에 포함하지 않습니다.  

앱 내 사용중인 클립아트코리아 폰트는 개인 및 기업 사용자에게 무료로 제공되며 상업적 사용이 가능하지만, 폰트 파일 자체의 수정, 복제 및 유료 판매는 금지됩니다.  

실제 운영 앱의 로고와 런처 아이콘은 직접 제작한 리소스이며, public 저장소에는 포함하지 않습니다.  
일부 UI 아이콘은 Google Material Icons를 사용했으며 Apache License 2.0을 따릅니다.
