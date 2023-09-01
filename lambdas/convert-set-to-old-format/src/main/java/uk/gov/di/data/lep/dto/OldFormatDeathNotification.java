package uk.gov.di.data.lep.dto;

public record OldFormatDeathNotification(
    OldFormatData data,   //TODO: List or not? leaving as not bc data included
    Object links, //TODO: don't care about
    Object meta //TODO: don't care about
) {
}
