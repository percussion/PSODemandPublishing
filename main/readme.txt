= PSODemandPublishing =

The PSODemandPublishing extension allows for a better publish now than what 6.1 provides.
The original publish now blocked when you a user used it. This version of publish now
will not block and will queue multiple publish now request for the same edition.
This package provides a workflow action, a custom content list generator 
and the demand publishing service. You can replace the custom content list generator
with your own but it should use the demand service to get the items that were selected
for publishing now.

If you would like a menu action for publish now you will have to write a
JSP that calls the demand publishing service (An example one will be 
added to the archive eventually).

For a better working example of demand publishing with more detailed instructions
see MLBPublishNow in the PSO source control repository
(http://foo/svn/psolib/customers/MLB/PublishNow/).

== Workflow Action Config ==

The config file is here RHYTHMYX_HOME/rxconfig/Workflow/demandpublish.xml.

The workflow action reads in a configuration file that 
maps workflow,transition,community,contenttype to a specific edition to be executed. 

The configuration file has the following format, it is an xml file: 

  <?xml version="1.0" encoding="UTF-8"?>
  <config>
      <rule>
          <transition>3</transition>
          <!-- edition is a required field -->
          <edition>310</edition>
      </rule>
      <rule>
          <workflow>5</workflow>
          <edition>320</edition>
      </rule>
      <rule>
          <transition>3</transition>
          <workflow>5</workflow>
          <edition>330</edition>
      </rule>
      <rule>
          <transition>3</transition>
          <workflow>5</workflow>
          <community>40</community>
          <edition>340</edition>
      </rule>
      <rule>
          <transition>3</transition>
          <workflow>5</workflow>
          <community>40</community>
          <contentType>300</contentType>
          <edition>350</edition>
      </rule>
  </config>
 
The best matching rule is picked when the workflow is executed. 


== Installation ==

 1. Unzip PSODemandPublishing.zip.
 2. Edit PSODemandPublishing/spring
    * Update the user/password for your cms publishing user.
 3. Change directory to PSODemandPublishing
 4. Run:
    ant -f deploy.xml install
 5. If you plan on using the workflow action create or edit the file:
    * RHYTHMYX_HOME/rxconfig/Workflow/demandpublish.xml
    * The schema is documented in the source and is very simple.



