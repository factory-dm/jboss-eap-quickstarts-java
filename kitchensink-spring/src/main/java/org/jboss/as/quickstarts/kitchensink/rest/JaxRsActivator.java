/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.as.quickstarts.kitchensink.rest;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Spring configuration class for REST endpoints.
 * 
 * This class replaces the original JAX-RS Activator that would define the application path.
 * In Spring Boot, REST controllers are automatically detected and configured, so we don't
 * need a specific activator class. The base path for REST endpoints is configured in 
 * application.yml via the server.servlet.context-path property.
 * 
 * All REST controllers in this package will be automatically detected and registered.
 */
@Configuration
@EnableWebMvc
public class JaxRsActivator {
    // This empty class serves as documentation and configuration placeholder
    // Spring Boot automatically detects and configures REST controllers
}
