/**
 * This module implements the Bento persistence database storage provider using
 * an H2 database configured using the Jakarta persistence API with Hikari for
 * thread pooling.
 *
 * @author Phil Bryant
 */
module bento.fx.persistence.storage.db.h2Database {

    requires transitive bento.fx.persistence.storage.db.common;

    requires com.zaxxer.hikari;
    requires jakarta.cdi.lang.model;
    requires jakarta.el;
    requires jakarta.transaction;
    requires net.bytebuddy;
    requires org.hibernate.orm.hikaricp;
    requires org.hibernate.validator;
}
