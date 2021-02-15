package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.UpdateProfileRequestDto;
import com.huellapositiva.application.dto.UpdateProposalRequestDto;
import com.huellapositiva.domain.dto.UpdateProfileResult;
import com.huellapositiva.domain.model.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.service.ProposalService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateProposalAction {

    public final ProposalService proposalService;

    public void execute(UpdateProposalRequestDto updateProposalRequestDto) {
        proposalService.updateProposal(updateProposalRequestDto);
        //UpdateProfileResult result = profileService.updateProfile(updateProfileRequestDto, accountId);
        /*if (result.isNewEmail()) {
            EmailConfirmation emailConfirmation = EmailConfirmation.from(updateProfileRequestDto.getEmail(), emailConfirmationBaseUrl);
            emailCommunicationService.sendMessageEmailChanged(emailConfirmation);
        }*/
    }

}
