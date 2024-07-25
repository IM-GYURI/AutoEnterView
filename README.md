# 💡 Auto Enter View

채용 프로세스 자동화 서비스

## 프로젝트 기간 : 2024.06.24 ~ 2024.07.30

## 프로젝트 기능 및 설계

### 1. 회원 관리

- 회원 가입 기능
  : 이메일, 비밀번호, 이름, 생년월일 정보 입력, 이메일 인증 (JavaMailSender)
  - 회원 로그인/로그아웃: JWT를 이용한 권한 관리 (Spring Security, JWT)
  - 비밀번호 찾기: 이름, 생년월일 입력 후 임시 비밀번호 발급 -> 바로 비밀번호를 변경할 수 있는 링크 같이 보내주기
  - 회원 정보 수정: 비밀번호 변경, 회원 탈퇴 기능(모든 정보 삭제)

### 2. 이력서 자동 필터링

## ERD

<img width="100%" src="doc/image/ERD.png" alt="ERD">

## Trouble Shooting

**[Go To Trouble Shooting](https://www.notion.so/b68b7782cb62448d93bbbb9225a45292)**

## Tech Stack

<div align=center> 
  <img src="https://img.shields.io/badge/java-007396?style=for-the-badge&logo=java&logoColor=white">
  <img src="https://img.shields.io/badge/springboot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">
  <img src="https://img.shields.io/badge/Spring Security-6DB33F?style=for-the-badge&logo=Spring Security&logoColor=white">
  <img src="https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jwt&logoColor=white">
  <img src="https://img.shields.io/badge/gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white">
  <img src="https://img.shields.io/badge/git-F05032?style=for-the-badge&logo=git&logoColor=white">
  <img src="https://img.shields.io/badge/github-181717?style=for-the-badge&logo=github&logoColor=white">
  <img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=Redis&logoColor=white">
  <img src="https://img.shields.io/badge/mariaDB-003545?style=for-the-badge&logo=mariaDB&logoColor=white"> 
  <img src="https://img.shields.io/badge/Elasticsearch-005571?style=for-the-badge&logo=Elasticsearch&logoColor=white">
  <img src="https://img.shields.io/badge/Kibana-005571?style=for-the-badge&logo=Kibana&logoColor=white">
  <img src="https://img.shields.io/badge/postman-FF6C37?style=for-the-badge&logo=postman&logoColor=white">
  <img src="https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=JUnit5&logoColor=white">
  <img src="https://img.shields.io/badge/swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=white">
</div>

## 개발 환경

- IDE : IntelliJ Ultimate
- Framework : Spring Boot 3.3.1
- Build Tool : Gradle
- Language : Java 17
- DataBase : Redis, MariaDB (JPA), Elasticsearch
- 라이브러리 : Lombok, JJWT, SpringDoc OpenAPI(Swagger), JUnit5, Mockito

## Flow

## 소프트웨어 아키텍처 다이어그램

## 개선 사항

## 프로젝트 후 느낀점 
