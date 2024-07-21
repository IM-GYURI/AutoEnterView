package com.ctrls.auto_enter_view.dto.resume;

import com.ctrls.auto_enter_view.entity.ResumeCareerEntity;
import com.ctrls.auto_enter_view.entity.ResumeCertificateEntity;
import com.ctrls.auto_enter_view.entity.ResumeEntity;
import com.ctrls.auto_enter_view.entity.ResumeExperienceEntity;
import com.ctrls.auto_enter_view.entity.ResumeTechStackEntity;
import com.ctrls.auto_enter_view.enums.TechStack;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class ResumeReadDto {

  @AllArgsConstructor
  @Getter
  public static class Response {

    private String resumeKey;
    private String candidateKey;
    private String title;
    private String jobWant;
    private String name;
    private String gender;
    private LocalDate birthDate;
    private String email;
    private String phoneNumber;
    private String address;
    private String education;
    private String schoolName;
    private String portfolio;
    private List<CareerDto> career;
    private List<CertificateDto> certificates;
    private List<ExperienceDto> experience;
    private List<String> techStack;
    private String resumeImageUrl;

    public static ResponseBuilder builder() {

      return new ResponseBuilder();
    }

    public static class ResponseBuilder {

      private String resumeKey;
      private String candidateKey;
      private String title;
      private String jobWant;
      private String name;
      private String gender;
      private LocalDate birthDate;
      private String email;
      private String phoneNumber;
      private String address;
      private String education;
      private String schoolName;
      private String portfolio;
      private List<CareerDto> career;
      private List<CertificateDto> certificates;
      private List<ExperienceDto> experience;
      private List<String> techStack;
      private String resumeImageUrl;

      ResponseBuilder() {

      }

      public ResponseBuilder entity(ResumeEntity resumeEntity) {

        resumeKey = resumeEntity.getResumeKey();
        candidateKey = resumeEntity.getCandidateKey();
        title = resumeEntity.getTitle();
        jobWant = resumeEntity.getJobWant().getValue();
        name = resumeEntity.getName();
        gender = resumeEntity.getGender();
        birthDate = resumeEntity.getBirthDate();
        email = resumeEntity.getEmail();
        phoneNumber = resumeEntity.getPhoneNumber();
        address = resumeEntity.getAddress();
        education = resumeEntity.getEducation().getValue();
        schoolName = resumeEntity.getSchoolName();
        portfolio = resumeEntity.getPortfolio();

        return this;
      }

      public ResponseBuilder resumeKey(String resumeKey) {

        this.resumeKey = resumeKey;
        return this;
      }

      public ResponseBuilder candidateKey(String candidateKey) {

        this.candidateKey = candidateKey;
        return this;
      }

      public ResponseBuilder title(String title) {

        this.title = title;
        return this;
      }

      public ResponseBuilder jobWant(String jobWant) {

        this.jobWant = jobWant;
        return this;
      }

      public ResponseBuilder name(String name) {

        this.name = name;
        return this;
      }

      public ResponseBuilder gender(String gender) {

        this.gender = gender;
        return this;
      }

      public ResponseBuilder birthDate(LocalDate birthDate) {

        this.birthDate = birthDate;
        return this;
      }

      public ResponseBuilder email(String email) {

        this.email = email;
        return this;
      }

      public ResponseBuilder phoneNumber(String phoneNumber) {

        this.phoneNumber = phoneNumber;
        return this;
      }

      public ResponseBuilder address(String address) {

        this.address = address;
        return this;
      }

      public ResponseBuilder education(String education) {

        this.education = education;
        return this;
      }

      public ResponseBuilder schoolName(String schoolName) {

        this.schoolName = schoolName;
        return this;
      }

      public ResponseBuilder portfolio(String portfolio) {

        this.portfolio = portfolio;
        return this;
      }

      public ResponseBuilder career(List<ResumeCareerEntity> career) {

        this.career = career.stream().map(e ->
            CareerDto.builder()
                .companyName(e.getCompanyName())
                .jobCategory(e.getJobCategory())
                .startDate(e.getStartDate())
                .endDate(e.getEndDate())
                .calculatedCareer(e.getCalculatedCareer())
                .build()
        ).collect(Collectors.toList());

        return this;
      }

      public ResponseBuilder certificates(List<ResumeCertificateEntity> certificates) {

        this.certificates = certificates.stream().map(e ->
            CertificateDto.builder()
                .certificateName(e.getCertificateName())
                .certificateDate(e.getCertificateDate())
                .build(
                )
        ).collect(Collectors.toList());

        return this;
      }

      public ResponseBuilder experience(List<ResumeExperienceEntity> experience) {

        this.experience = experience.stream().map(e ->
            ExperienceDto.builder()
                .experienceName(e.getExperienceName())
                .startDate(e.getStartDate())
                .endDate(e.getEndDate())
                .build(
                )
        ).collect(Collectors.toList());

        return this;
      }

      public ResponseBuilder techStack(List<ResumeTechStackEntity> techStack) {

        this.techStack = techStack.stream()
            .map(ResumeTechStackEntity::getTechStackName)
            .map(TechStack::getValue)
            .collect(Collectors.toList());

        return this;
      }

      public ResponseBuilder image(String resumeImageUrl) {

        this.resumeImageUrl = resumeImageUrl;
        return this;
      }

      public Response build() {

        return new Response(this.resumeKey, this.candidateKey, this.title, this.jobWant, this.name,
            this.gender, this.birthDate, this.email, this.phoneNumber, this.address,
            this.education, this.schoolName, this.portfolio, this.career, this.certificates,
            this.experience, this.techStack, this.resumeImageUrl);
      }

      public String toString() {

        return "ResumeReadDto.Response.ResponseBuilder(resumeKey=" + this.resumeKey
            + ", candidateKey=" + this.candidateKey + ", title=" + this.title + ", jobWant="
            + this.jobWant + ", name=" + this.name + ", gender=" + this.gender + ", birthDate="
            + this.birthDate + ", email=" + this.email + ", phoneNumber=" + this.phoneNumber
            + ", address=" + this.address + ", education=" + this.education + ", schoolName="
            + this.schoolName + ", portfolio=" + this.portfolio + ", career=" + this.career
            + ", certificates=" + this.certificates + ", experience=" + this.experience
            + ", techStack=" + this.techStack + ", image=" + this.resumeImageUrl + ")";
      }
    }
  }
}