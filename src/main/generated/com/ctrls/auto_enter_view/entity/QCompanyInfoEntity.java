package com.ctrls.auto_enter_view.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QCompanyInfoEntity is a Querydsl query type for CompanyInfoEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCompanyInfoEntity extends EntityPathBase<CompanyInfoEntity> {

    private static final long serialVersionUID = -678495640L;

    public static final QCompanyInfoEntity companyInfoEntity = new QCompanyInfoEntity("companyInfoEntity");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final StringPath address = createString("address");

    public final StringPath boss = createString("boss");

    public final DatePath<java.time.LocalDate> companyAge = createDate("companyAge", java.time.LocalDate.class);

    public final StringPath companyInfoKey = createString("companyInfoKey");

    public final StringPath companyKey = createString("companyKey");

    public final StringPath companyName = createString("companyName");

    public final StringPath companyUrl = createString("companyUrl");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> employees = createNumber("employees", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QCompanyInfoEntity(String variable) {
        super(CompanyInfoEntity.class, forVariable(variable));
    }

    public QCompanyInfoEntity(Path<? extends CompanyInfoEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCompanyInfoEntity(PathMetadata metadata) {
        super(CompanyInfoEntity.class, metadata);
    }

}

