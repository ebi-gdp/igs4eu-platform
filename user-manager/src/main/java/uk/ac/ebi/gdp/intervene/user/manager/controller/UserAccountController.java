/*
 *
 * Copyright 2022 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package uk.ac.ebi.gdp.intervene.user.manager.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.gdp.intervene.user.manager.dto.UserAccountDTO;
import uk.ac.ebi.gdp.intervene.user.manager.exception.ServiceException;
import uk.ac.ebi.gdp.intervene.user.manager.exception.SystemException;
import uk.ac.ebi.gdp.intervene.user.manager.exception.UserAccountAlreadyExistsException;
import uk.ac.ebi.gdp.intervene.user.manager.exception.UserAccountCreationException;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.entity.AuthUserAccount;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.entity.UserAccount;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.service.IUserAccountPersistenceService;
import uk.ac.ebi.gdp.intervene.user.manager.service.IUserManagerService;

import static org.springframework.http.ResponseEntity.ok;

@RequestMapping("/account")
@RestController
public class UserAccountController {

    private final IUserManagerService userManagerService;
    private final IUserAccountPersistenceService userAccountPersistenceService;

    public UserAccountController(final IUserManagerService userManagerService,
                                 final IUserAccountPersistenceService userAccountPersistenceService) {
        this.userManagerService = userManagerService;
        this.userAccountPersistenceService = userAccountPersistenceService;
    }

    @GetMapping
    public ResponseEntity<UserAccountDTO> getUserAccount(final @AuthenticationPrincipal Jwt principal) {
        final AuthUserAccount authUserAccount = userAccountPersistenceService
                .getUserAccount(principal.getSubject())
                .orElseThrow(() -> ServiceException
                        .resourceNotFound(String.format("User account having auth id %s not found",
                                principal.getSubject())));
        return ok(buildUserAccountDTO(authUserAccount.getUserAccount()));
    }

    @PostMapping
    public ResponseEntity<UserAccountDTO> createUserAccount(final @AuthenticationPrincipal Jwt principal) {
        //Get existing entity
        try {
            final UserAccount userAccount = userManagerService.createUserAccount(principal.getSubject());
            return ok(buildUserAccountDTO(userAccount));
        } catch (UserAccountAlreadyExistsException e) {
            throw ServiceException.dataConflict(e.getMessage());
        } catch (UserAccountCreationException e) {
            throw new SystemException(e.getMessage());
        }
    }

    private UserAccountDTO buildUserAccountDTO(final UserAccount userAccount) {
        return new UserAccountDTO(
                userAccount.getUserId(),
                userAccount.getGivenName(),
                userAccount.getFamilyName(),
                userAccount.getEmailId()
        );
    }
}
