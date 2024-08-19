package com.ctrls.auto_enter_view.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QJobPostingEntity is a Querydsl query type for JobPostingEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QJobPostingEntity extends EntityPathBase<JobPostingEntity> {

    private static final long serialVersionUID = -308060690L;

    public static final QJobPostingEntity jobPostingEntity = new QJobPostingEntity("jobPostingEntity");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final NumberPath<Integer> career = createNumber("career", Integer.class);

    public final StringPath companyKey = createString("companyKey");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final EnumPath<com.ctrls.auto_enter_view.enums.Education> education = createEnum("education", com.ctrls.auto_enter_view.enums.Education.class);

    public final StringPath employmentType = createString("employmentType");

    public final DatePath<java.time.LocalDate> endDate = createDate("endDate", java.time.LocalDate.class);

    public final EnumPath<com.ctrls.auto_enter_view.enums.JobCategory> jobCategory = createEnum("jobCategory", com.ctrls.auto_enter_view.enums.JobCategory.class);

    public final StringPath jobPostingContent = createString("jobPostingContent");

    public final StringPath jobPostingKey = createString("jobPostingKey");

    public final NumberPath<Integer> passingNumber = createNumber("passingNumber", Integer.class);

    public final NumberPath<Long> salary = createNumber("salary", Long.class);

    public final DatePath<java.time.LocalDate> startDate = createDate("startDate", java.time.LocalDate.class);

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final StringPath workLocation = createString("workLocation");

    public final StringPath workTime = createString("workTime");

    public QJobPostingEntity(String variable) {
        super(JobPostingEntity.class, forVariable(variable));
    }

    public QJobPostingEntity(Path<? extends JobPostingEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QJobPostingEntity(PathMetadata metadata) {
        super(JobPostingEntity.class, metadata);
    }

}

