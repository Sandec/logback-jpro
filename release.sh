if [[ "$JAVA_HOME" != *"temurin-11.jdk"* ]]; then
  echo "###################################################"
  echo "### Please use Temurin version 11 for the build ###"
  echo "###################################################"
  say "Use Java 11"
  exit 1
fi

mvn versions:set -DgenerateBackupPoms=false -DnewVersion=1.4.8-jpro

mvn clean
mvn install -DskipTests=true
mvn animal-sniffer:check
#mvn site:site

#mvn javadoc:jar
#mvn assembly:single

#export GPG_TTY=$(tty)
#password
#mvn deploy -P javadocjar,sign-artifacts -Dgpg.passphrase=passwd
mvn deploy -DskipTests=true -P javadocjar


# cleanHistory dep
#uncomment diffie-hellman support in /etc/ssh/sshd_config

#mvn site:deploy -N # with Java 8!!!

#git tag -m "tagging" -a v_${VERSION_NUMBER}
#git push --tags

#release version and add next version on jira
