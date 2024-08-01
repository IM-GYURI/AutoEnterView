


<br>
<br>

<h1> 💡 Auto Enter View </h1>

채용 과정의 자동화, Auto Enter View와 함께해보세요. <br>
서류 심사부터 면접 일정 관리까지 자동으로 해결하고, 채용의 전 과정을 간편하게 만들어 드립니다.

<br>

<img src="doc/image/main.png" alt="main page" width="100%">

<br>
<br>

## 🗓️ Schedule
프로젝트 전체 진행 기간 : 2024.06.24 ~ 2024.07.31

<br>

## 🔗 Project Links


#### 🚀 [Auto Enter View](https://auto-enterview-fe.vercel.app/) : 서비스 배포

#### 🧾 [Auto Enter View](https://gray-heather-95a.notion.site/AutoEnterView-bad9c322c8104fc8bd86f4f86c6c7291) : Notion

#### 🖥️ [Database Schema](https://www.erdcloud.com/d/kMYDqc92D3iuqZGgL) : ERD (Entity Relationship Diagram)

#### 👻 [Frontend Repository](https://github.com/cheonjiyun/auto-enterview-fe) : Frontend GitHub


<br>
<br>

## 🧑‍🤝‍🧑 Backend Members

<table style="width: 100%; margin-left: auto; margin-right: auto;">
  <tr>
    <td>
      <a href="https://github.com/Goldbar97">
        <img src="doc/image/Logo.png" width="120px" height="120px" alt="성준"/>
      </a>  
    </td>
    <td>
      <a href="https://github.com/IM-GYURI">
        <img src="doc/image/gyuri.jpg" width="120px" height="120px" alt="규리"/>
      </a>  
    </td>
    <td>
      <a href="https://github.com/ESJung95">
        <img src="doc/image/eunsun.jpg" width="120px" height="120px" alt="은선"/>
      </a>  
    </td>
    <td>
      <a href="https://github.com/GLORY-JI">
        <img src="doc/image/glory.png" width="120px" height="120px" alt="영광"/>
      </a>  
    </td>
  </tr>

  <tr>
    <th>강성준</th>
    <th>임규리</th>
    <th>정은선</th>
    <th>지영광</th>
  </tr>

  <tr>
    <th>BE</th>
    <th>BE</th>
    <th>👑 BE</th>
    <th>BE</th>
  </tr>
</table>

<br>
<br>

## 📝 프로젝트 기능 및 설계

### 1.공통 기능

| 기능 | 세부 사항 |
|------|-----------|
| 👤 **회원 관리** | • 회원 유형 선택 (회사/지원자)<br>• 이메일 중복 확인<br>• 이메일 인증<br>• 비밀번호 입력 (눈 아이콘으로 확인 가능)<br>• 로그인/로그아웃<br>• 회원 정보 수정 및 삭제 |
| 🔒 **보안** | • 로그아웃 시 토큰 블랙리스트 관리 |

### 2. 회사 기능

| 기능 | 세부 사항 |
|------|-----------|
| 📊 **회사 정보 관리** | • 정보 등록 (사원 수, 설립 년도, 홈페이지 URL, 설명, 대표자, 주소)<br>• 정보 수정 및 삭제 |
| 📢 **채용공고 관리** | • 공고 등록, 수정, 삭제<br>• 채용 기간 설정<br>• 채용 절차 단계 설정 (서류 단계 필수) |
| 👥 **지원자 관리** | • 이력서 자동 필터링<br>• 서류 합격자 자동 선별<br>• 채용 프로세스 관리<br>• 면접 일정 자동 생성<br>• 합격자 알림 및 면접 일정 알림<br>• 지원자 목록 및 현황 조회 |

### 3. 지원자 기능

| 기능 | 세부 사항 |
|------|-----------|
| 🔑 **계정 관리** | • 아이디 찾기<br>• 임시 비밀번호 발급 |
| 📄 **이력서 관리** | • 이력서 등록, 수정, 삭제<br>• 상세 정보 입력 (학력, 경력, 보유 기술, 자격증 등) |
| 🔍 **채용공고 관리** | • 전체 채용공고 조회<br>• 맞춤 채용공고 조회<br>• 채용공고 지원<br>• 지원 현황 조회 |
| 📅 **면접 관리** | • 면접 일정 조회 |

<br>
<br>

## 🌲 개발 환경
- **IDE** : IntelliJ Ultimate <br>
- **Framework** : Spring Boot 3.3.1 <br>
- **Build Tool** : Gradle <br>
- **Language** : Java 17 <br>
- **DataBase** : Redis, MariaDB (JPA) <br>
- **CI/CD** : Docker, Github Actions <br>
- **Cloud Services** : AWS EC2, AWS Route 53, Amazon Certificate Manager, Amazon ELB, Amazon IAM, Amazon S3 <br>
- **Library** : Spring mail, Spring quartz, Spring validation, Lombok, JJWT, SpringDoc OpenAPI(Swagger), JUnit5, Mockito <br>

<br>

## 🛠️ Backend Tech Stack
<table style="margin-left: auto; margin-right: auto;">
  <tr>
    <td><h4>Language</h4></td>
    <td>
      <img src="https://img.shields.io/badge/java-007396?style=flat-square&logo=java&logoColor=white" alt="Java">
    </td>
  </tr>
  <tr>
    <td><h4>Framework</h4></td>
    <td>
      <img src="https://img.shields.io/badge/springboot-6DB33F?style=flat-square&logo=springboot&logoColor=white" alt="Spring Boot">
    </td>
  </tr>
  <tr>
    <td><h4>Database</h4></td>
    <td>
      <img src="https://img.shields.io/badge/mariaDB-003545?style=flat-square&logo=mariaDB&logoColor=white" alt="MariaDB">
      <img src="https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=Redis&logoColor=white" alt="Redis">
    </td>
  </tr>
  <tr>
    <td><h4>ORM</h4></td>
    <td>
      <img src="https://img.shields.io/badge/Spring%20Data%20JPA-6DB33F?style=flat-square&logo=spring&logoColor=white" alt="Spring Data JPA">
    </td>
  </tr>
  <tr>
    <td><h4>Build Tool</h4></td>
    <td>
      <img src="https://img.shields.io/badge/gradle-02303A?style=flat-square&logo=gradle&logoColor=white" alt="Gradle">
    </td>
  </tr>
  <tr>
    <td><h4>Testing</h4></td>
    <td>
      <img src="https://img.shields.io/badge/JUnit5-25A162?style=flat-square&logo=JUnit5&logoColor=white" alt="JUnit5">
      <img src="https://img.shields.io/badge/Mockito-25A162?style=flat-square&logo=mockito&logoColor=white" alt="Mockito">
    </td>
  </tr>
  <tr>
    <td><h4>Version Control</h4></td>
    <td>
      <img src="https://img.shields.io/badge/git-F05032?style=flat-square&logo=git&logoColor=white" alt="Git">
      <img src="https://img.shields.io/badge/github-181717?style=flat-square&logo=github&logoColor=white" alt="GitHub">
    </td>
  </tr>
  <tr>
    <td><h4>CI/CD</h4></td>
    <td>
      <img src="https://img.shields.io/badge/docker-2496ED?style=flat-square&logo=docker&logoColor=white" alt="Docker">
      <img src="https://img.shields.io/badge/Github%20Actions-2088FF?style=flat-square&logo=github-actions&logoColor=white" alt="GitHub Actions">
    </td>
  </tr>
  <tr>
    <td><h4>Cloud</h4></td>
    <td>
      <img src="https://img.shields.io/badge/AWS-232F3E?style=flat-square&logo=amazonwebservices&logoColor=white" alt="AWS">
      <img src="https://img.shields.io/badge/Amazon%20S3-569A31?style=flat-square&logo=amazons3&logoColor=white" alt="Amazon S3">
      <img src="https://img.shields.io/badge/Amazon%20EC2-FF9900?style=flat-square&logo=amazonec2&logoColor=white" alt="Amazon EC2">
      <img src="https://img.shields.io/badge/Amazon%20Route%2053-8C4FFF?style=flat-square&logo=amazonroute53&logoColor=white" alt="Amazon Route 53">
      <img src="https://img.shields.io/badge/Amazon%20ELB-FF9900?style=flat-square&logo=amazonaws&logoColor=white" alt="Amazon ELB">
    </td>
  </tr>
  <tr>
    <td><h4>API Documentation</h4></td>
    <td>
      <img src="https://img.shields.io/badge/swagger-85EA2D?style=flat-square&logo=swagger&logoColor=black" alt="Swagger">
    </td>
  </tr>
  <tr>
    <td><h4>Authentication</h4></td>
    <td>
      <img src="https://img.shields.io/badge/JWT-000000?style=flat-square&logo=json-web-tokens&logoColor=white" alt="JWT">
      <img src="https://img.shields.io/badge/Spring%20Security-6DB33F?style=flat-square&logo=Spring%20Security&logoColor=white" alt="Spring Security">
    </td>
  </tr>
  <tr>
    <td><h4>IDE</h4></td>
    <td>
      <img src="https://img.shields.io/badge/IntelliJ%20IDEA-000000?style=flat-square&logo=intellij-idea&logoColor=white" alt="IntelliJ IDEA">
    </td>
  </tr>
</table>

<br>
<br>

## ⛓️ Architecture
<img width="100%" src="doc/image/Architecture.png" alt="ERD">

<br>
<br>

## 🖥️ ERD
<img width="100%" src="doc/image/ERD.png" alt="ERD">

<br>
<br>

## 📽️ 시연 영상

|              회원가입/로그인/로그아웃               |               이메일 찾기/비밀번호 변경/회원 탈퇴               |                   메인 페이지/상세 페이지                    |                    회사 - 마이페이지/채용공고CRUD                    |  
|:----------------------------------------:|:------------------------------------------------:|:--------------------------------------------------:|:---------------------------------------------------------:|
|  ![회원가입/로그인/로그아웃](doc/video/signup.gif)  | ![이메일 찾기/비밀번호 변경/회원 탈퇴](doc/video/findEmail.gif) |        ![메인/상세 페이지](doc/video/mainPage.gif)        |       ![회사 - 마이페이지/채용공고](doc/video/jobPosting.gif)        |
|           응시자 - 마이페이지/이력서CRUD            |                     채용단계 관리                      |                   일정 관리/메일 발송 예약                   |                                                           |
| ![응시자 - 마이페이지/이력서](doc/video/resume.gif) |     ![채용단계 관리](doc/video/jobPostingStep.gif)     | ![일정 관리/메일 발송 예약](doc/video/interviewSchedule.gif) |                                                           |

<br>
<br>

## ♾️ Flow

### 1. 일정 생성

<img width="90%" src="doc/image/interviewScheduleFlow.png" alt="interviewScheduleFlow">

### 2. 이력서 필터링

<img width="90%" src="doc/image/filteringFlow.png" alt="filteringFlow">
<br>
<img width="90%" src="doc/image/filteringImage.png" alt="filteringImage">

<br>
<br>

## 🎯 Trouble Shooting
**➡ [Go To Trouble Shooting](https://www.notion.so/b68b7782cb62448d93bbbb9225a45292)**

<br>
<br>

##  ✏️ ️개선 사항

<br>
<br>

## 🚩 프로젝트 후 느낀점
- 성준
- 규리
- 은선
- 영광

<br>
<br>

## 🙌 전체 시연 영상
[![전체 시연 영상](doc/image/thumbnail.png)](https://drive.google.com/file/d/1EYCoW52AOYpITab21-rw6Xamw4sf8qhk/view?usp=sharing)