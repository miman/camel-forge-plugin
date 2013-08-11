@/* This is a test script demonstrating the creation of a project with these forge plugins */;
@/* run this by starting a forge console & write 'run miman-prj-example.fsh' */;
@/* OBS ! For some reason the script hangs when running in the eclipse shell, but works fine in a separate console window. */;

@/* Clear the screen */;
clear;

@/* This means less typing. If a script is automated, or is not meant to be interactive, use this command */;
set ACCEPT_DEFAULTS true;

@/* Create root project */;
new-project --named camel-m-test-prj --topLevelPackage se.comp.test;
miman-prj setup;

new-project --named testprj-war --topLevelPackage se.comp.test.web;
camel-web setup --camelBasePackage se.comp.test --restletSupport true;

cd ..;
new-project --named routes --type pom;
miman-reactor-prj setup;

new-project --named test-route --topLevelPackage se.comp.test.testroute;
camel-route setup --warProjectArtifactId testprj-war --addTestRoute true;

@/* Go back to the root directory */;
cd ..;
cd ..;

@/* Test run with 'mvn jetty:run' in the web project */;