package uk.gov.gdx.datashare.controllers

import com.amazonaws.xray.spring.aop.XRayEnabled
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated

@Validated
@XRayEnabled
@Transactional
abstract class BaseApiController
