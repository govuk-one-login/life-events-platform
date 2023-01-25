package uk.gov.gdx.datashare.helper

import org.springframework.hateoas.IanaLinkRelations.*
import org.springframework.hateoas.Link
import org.springframework.hateoas.LinkRelation
import org.springframework.hateoas.PagedModel.PageMetadata
import org.springframework.web.util.UriComponentsBuilder

private const val PAGE_SIZE_PARAM = "page[size]"
private const val PAGE_NUMBER_PARAM = "page[number]"

fun getPageLinks(pageMetadata: PageMetadata, uriBuilder: UriComponentsBuilder): List<Link> {
  uriBuilder.replaceQueryParam(PAGE_SIZE_PARAM, pageMetadata.size)
  val pageLinks = mutableListOf<Link>()
  if (pageMetadata.number > 0) {
    pageLinks.add(firstLink(uriBuilder))
    pageLinks.add(prevLink(uriBuilder, pageMetadata))
  }
  if (pageMetadata.number < pageMetadata.totalPages - 1) {
    pageLinks.add(nextLink(uriBuilder, pageMetadata))
    pageLinks.add(lastLink(uriBuilder, pageMetadata))
  }

  return pageLinks
}

private fun firstLink(
  uriBuilder: UriComponentsBuilder,
): Link = pageLink(uriBuilder, 0, FIRST)

private fun prevLink(
  uriBuilder: UriComponentsBuilder,
  pageMetadata: PageMetadata,
): Link = pageLink(uriBuilder, pageMetadata.number - 1, PREV)

private fun nextLink(
  uriBuilder: UriComponentsBuilder,
  pageMetadata: PageMetadata,
): Link = pageLink(uriBuilder, pageMetadata.number + 1, NEXT)

private fun lastLink(
  uriBuilder: UriComponentsBuilder,
  pageMetadata: PageMetadata,
): Link = pageLink(uriBuilder, pageMetadata.totalPages - 1, LAST)

private fun pageLink(uriBuilder: UriComponentsBuilder, pageNumber: Long, rel: LinkRelation) =
  Link.of(
    uriBuilder
      .replaceQueryParam(PAGE_NUMBER_PARAM, pageNumber)
      .toUriString(),
  ).withRel(rel)
