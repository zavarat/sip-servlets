/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.deployment;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.deployer.SchemaResolverDeployer;
import org.jboss.metadata.sip.jboss.JBossConvergedSipMetaData;
import org.jboss.metadata.sip.spec.SipMetaData;
import org.jboss.metadata.web.spec.WebMetaData;

/**
 * An ObjectModelFactoryDeployer for translating jboss-web.xml descriptors into
 * WebMetaData instances.
 * 
 * @author Scott.Stark@jboss.org
 * @author adrian@jboss.org
 * @version $Revision:$
 */
public class JBossConvergedSipAppParsingDeployer extends SchemaResolverDeployer<JBossConvergedSipMetaData>
{
   /**
    * Create a new JBossWebAppParsingDeployer.
    */
   public JBossConvergedSipAppParsingDeployer()
   {
      super(JBossConvergedSipMetaData.class);
      addInput(WebMetaData.class);
      addInput(SipMetaData.class);
      setName("jboss-web.xml");
   }

   /**
    * Get the virtual file path for the jboss-web descriptor in the
    * DeploymentContext.getMetaDataPath.
    * 
    * @return the current virtual file path for the web-app descriptor
    */
   public String getWebXmlPath()
   {
      return getName();
   }
   /**
    * Set the virtual file path for the jboss-web descriptor in the
    * DeploymentContext.getMetaDataLocation. The standard path is jboss-web.xml
    * to be found in the WEB-INF metdata path.
    * 
    * @param webXmlPath - new virtual file path for the web-app descriptor
    */
   public void setWebXmlPath(String webXmlPath)
   {
      setName(webXmlPath);
   }

   @Override
   protected void createMetaData(DeploymentUnit unit, String name, String suffix) throws DeploymentException
   {
      super.createMetaData(unit, name, suffix);
      // Merge the spec metadata
      WebMetaData specMetaData = unit.getAttachment(WebMetaData.class);
      SipMetaData sipMetaData = unit.getAttachment(SipMetaData.class);
      JBossConvergedSipMetaData metaData = unit.getAttachment(JBossConvergedSipMetaData.class);
      if(specMetaData == null && sipMetaData ==null && metaData == null)
         return;

      // If there no JBossConvergedSipMetaData was created from a jboss-web.xml, create one
      if (metaData == null)
      {
         metaData = new JBossConvergedSipMetaData();
      }      
      // Create a merged view
      JBossConvergedSipMetaData mergedMetaData = new JBossConvergedSipMetaData();
      mergedMetaData.merge(metaData, specMetaData);
      mergedMetaData.merge(metaData, sipMetaData);
      // Set the merged as the output
      unit.getTransientManagedObjects().addAttachment(JBossConvergedSipMetaData.class, mergedMetaData);
      // Keep the raw parsed metadata as well
      unit.addAttachment("Raw"+JBossConvergedSipMetaData.class.getName(), mergedMetaData, JBossConvergedSipMetaData.class);
   }

   /**
    * Make sure we always have a JBossConvergedSipMetaData object attached, even if there is no jboss-web.xml
    * in the deployment
    */
   @Override
   protected void createMetaData(DeploymentUnit unit, String name, String suffix, String key) throws DeploymentException
   {
      super.createMetaData(unit, name, suffix, key);
      
      WebMetaData wmd = unit.getTransientManagedObjects().getAttachment(WebMetaData.class);
      SipMetaData smd = unit.getTransientManagedObjects().getAttachment(SipMetaData.class);
      JBossConvergedSipMetaData result = unit.getTransientManagedObjects().getAttachment(getOutput());
      if (result == null && wmd != null && smd != null)
      {
         result = new JBossConvergedSipMetaData();
         result.merge(null, wmd);
         result.merge(wmd, smd);
         unit.getTransientManagedObjects().addAttachment(key, result, getOutput());
      }
      if (result == null && wmd != null)
      {
         result = new JBossConvergedSipMetaData();
         result.merge(null, wmd);
         unit.getTransientManagedObjects().addAttachment(key, result, getOutput());
      }
      if (result == null && smd != null)
      {
         result = new JBossConvergedSipMetaData();
         result.merge(null, smd);
         unit.getTransientManagedObjects().addAttachment(key, result, getOutput());
      }
   }
}