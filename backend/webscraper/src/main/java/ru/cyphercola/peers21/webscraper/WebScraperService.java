// package ru.cyphercola.peers21.webscraper;
//
// import org.htmlunit.WebClient;
// import org.htmlunit.html.HtmlElement;
// import org.htmlunit.html.HtmlForm;
// import org.htmlunit.html.HtmlPage;
// import org.htmlunit.html.HtmlSubmitInput;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.context.properties.EnableConfigurationProperties;
// import org.springframework.stereotype.Service;
//
// import java.io.IOException;
// import java.net.MalformedURLException;
// import java.util.Iterator;
// import java.util.List;
//
// @Service
// @EnableConfigurationProperties(ExternalApiRequestServiceProperties.class)
// public class WebScraperService {
//   private final String websiteBaseUrl;
//   private final String authPageUrl;
//   private final String apiUsername;
//   private final String apiPassword;
//
//   public WebScraperService(@Autowired ExternalApiRequestServiceProperties properties) {
//     this.websiteBaseUrl = properties.websiteUrl();
//     this.authPageUrl = properties.websiteAuthUrl();
//     this.apiUsername = properties.apiUsername();
//     this.apiPassword = properties.apiPassword();
//   }
//
//   public List<String> getElementInnerText(List<String> urls, String elementId) {
//     try (WebClient client = new WebClient()) {
//       HtmlPage websitePage = client.getPage(websiteBaseUrl);
//       if (websitePage.getUrl().toString().contains(authPageUrl)) {
//         HtmlForm form = websitePage.getForms().getFirst();
//         Iterator<HtmlElement> inputs = form.getFormElements().iterator();
//         inputs.next().type(apiUsername);
//         inputs.next().type(apiPassword);
//         inputs.next().click();
//       }
//       return urls.stream().map(url -> getElementInnerText(url, elementId)).toList();
//     } catch (IOException e) {
//       throw new RuntimeException(e);
//     }
//   }

  // private String getElementInnerText(String url, String elementId) {
  //
  // }
// }
