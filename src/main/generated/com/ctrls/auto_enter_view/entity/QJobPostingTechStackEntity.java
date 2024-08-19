package com.ctrls.auto_enter_view.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QJobPostingTechStackEntity is a Querydsl query type for JobPostingTechStackEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QJobPostingTechStackEntity extends EntityPathBase<JobPostingTechStackEntity> {

    private static final long serialVersionUID = -199476630L;

    public static final QJobPostingTechStackEntity jobPostingTechStackEntity = new QJobPostingTechStackEntity("jobPostingTechStackEntity");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath jobPostingKey = createString("jobPostingKey");

    public final EnumPath<com.ctrls.auto_enter_view.enums.TechStack> techName = createEnum("techName", com.ctrls.auto_enter_view.enums.TechStack.class);

    public QJobPostingTechStackEntity(String variable) {
        super(JobPostingTechStackEntity.class, forVariable(variable));
    }

    public QJobPostingTechStackEntity(Path<? extends JobPostingTechStackEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QJobPostingTechStackEntity(PathMetadata metadata) {
        super(JobPostingTechStackEntity.class, metadata);
    }

}

