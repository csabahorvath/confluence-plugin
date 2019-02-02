package com.csabahorvath.pagecreatelistener.impl;

import com.atlassian.confluence.event.events.content.page.PageCreateEvent;
import com.atlassian.confluence.event.events.content.page.PageUpdateEvent;
import com.atlassian.confluence.labels.Label;
import com.atlassian.confluence.labels.LabelManager;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.util.LabelUtil;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.spring.scanner.annotation.imports.ConfluenceImport;
import com.atlassian.user.EntityException;
import com.atlassian.user.Group;
import com.atlassian.user.GroupManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class PageCreateEventListener implements InitializingBean, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(PageCreateEventListener.class);

    @ConfluenceImport
    private final EventPublisher eventPublisher;

    @ConfluenceImport
    private final LabelManager labelManager;

    @ConfluenceImport
    private final GroupManager groupManager;

    @Inject
    public PageCreateEventListener(EventPublisher eventPublisher, LabelManager labelManager, GroupManager groupManager) {
        this.eventPublisher = eventPublisher;
        this.labelManager = labelManager;
        this.groupManager = groupManager;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        eventPublisher.register(this);
    }

    @Override
    public void destroy() throws Exception {
        eventPublisher.unregister(this);
    }

    @EventListener
    public void onPageCreateEvent(PageCreateEvent event) {
        try {
            LOGGER.warn("Adding user group labels to page...");

            ConfluenceUser confluenceUser = AuthenticatedUserThreadLocal.get();
            groupManager.getGroups(confluenceUser)
                    .forEach(group -> addLabelToPageByGroup(event.getPage(), group));

            LOGGER.warn("Adding user group labels to page finished!");
        } catch (EntityException ex) {
            LOGGER.error("Error during adding labels to page!", ex);
        }
    }


    /*@EventListener
    public void onPageUpdateEvent(PageUpdateEvent event) {
        try {
            LOGGER.warn("Checking user group labels of page...");

            ConfluenceUser confluenceUser = AuthenticatedUserThreadLocal.get();
            groupManager.getGroups(confluenceUser)
                    .forEach(group -> addLabelToPageByGroup(event.getPage(), group));

            LOGGER.warn("Checking user group labels of page finished!");
        } catch (EntityException ex) {
            LOGGER.error("Error during checking labels of page!", ex);
        }
    }*/

    private void addLabelToPageByGroup(Page page, Group group) {
        String newLabelName = getLabelNameByGroup(group);
        if (newLabelName.isEmpty()) {
            return;
        }

        if (!isNewLabel(newLabelName, page)) {
            return;
        }

        Label label = LabelUtil.addLabel(newLabelName, labelManager, page);
        if (label != null) {
            LOGGER.warn("Label added: " + label.getName());
        } else {
            LOGGER.warn("Could not add label! Name: " + newLabelName);
        }
    }

    private String getLabelNameByGroup(Group group) {
        return group.getName().replace(" ", "-");
    }

    private boolean isNewLabel(String labelName, Page page) {
        return page.getLabels().stream().noneMatch(l -> labelName.equals(l.getName()));
    }

}
