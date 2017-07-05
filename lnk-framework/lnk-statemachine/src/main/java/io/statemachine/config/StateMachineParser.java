package io.statemachine.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import io.statemachine.spi.def.SmpStateMachine;
import io.statemachine.spi.def.SmpStateMachineExecutor;
import io.statemachine.spi.diagram.def.SmpDiagram;
import io.statemachine.spi.diagram.def.SmpState;
import io.statemachine.utils.FieldRetriever;
import io.statemachine.utils.LnkComponentUtils;
import io.statemachine.utils.LnkComponentUtils.ComponentCallback;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年1月1日 下午4:39:48
 */
public class StateMachineParser implements BeanDefinitionParser {

    protected static final Logger log = LoggerFactory.getLogger(StateMachineParser.class.getSimpleName());

    @Override
    public BeanDefinition parse(Element rootElement, ParserContext parserContext) {
        final String stateMachineId = rootElement.getAttribute("id");
        final String statemachineExecutorId = stateMachineId.concat("Executor");
        final String handlerExceptionResolverId = rootElement.getAttribute("handler-exception-resolver");
        final String asyncTaskExecutorId = rootElement.getAttribute("async-task-executor");
        Element stateDiagramsElement = DomUtils.getChildElementByTagName(rootElement, "state-diagrams");
        List<Element> stateDiagramElements = DomUtils.getChildElementsByTagName(stateDiagramsElement, "state-diagram");
        final ManagedList<Object> diagramsManagedList = new ManagedList<Object>();
        if (CollectionUtils.isEmpty(stateDiagramElements) == false) {
            for (Element stateDiagramElement : stateDiagramElements) {
                final String stateDiagramId = stateDiagramElement.getAttribute("id");
                final String resourcesStateLoaderId = stateDiagramElement.getAttribute("resources-state-loader");
                final String eventType = stateDiagramElement.getAttribute("event-type");
                final String stateType = stateDiagramElement.getAttribute("state-type");
                List<Element> stateElements = DomUtils.getChildElementsByTagName(stateDiagramElement, "state");
                if (CollectionUtils.isEmpty(stateElements) == false) {
                    final ManagedList<Object> statesManagedList = new ManagedList<Object>();
                    for (Element stateElement : stateElements) {
                        final String event = stateElement.getAttribute("event");
                        final String source = stateElement.getAttribute("source");
                        final String target = stateElement.getAttribute("target");
                        final String taskRef = stateElement.getAttribute("task-ref");
                        LnkComponentUtils.parse(event, SmpState.class, rootElement, parserContext, new ComponentCallback() {
                            public void onParse(RootBeanDefinition beanDefinition) {
                                try {
                                    beanDefinition.getPropertyValues().addPropertyValue("event", FieldRetriever.getObject(eventType + "." + event));
                                    beanDefinition.getPropertyValues().addPropertyValue("source", FieldRetriever.getObject(stateType + "." + source));
                                    beanDefinition.getPropertyValues().addPropertyValue("target", FieldRetriever.getObject(stateType + "." + target));
                                    beanDefinition.getPropertyValues().addPropertyValue("task", new RuntimeBeanReference(taskRef));
                                } catch (Throwable e) {
                                    log.error("parse event " + event + " Error.", e);
                                }
                            }
                        });
                        statesManagedList.add(new RuntimeBeanReference(event));
                    }
                    LnkComponentUtils.parse(stateDiagramId, SmpDiagram.class, rootElement, parserContext, new ComponentCallback() {
                        public void onParse(RootBeanDefinition beanDefinition) {
                            beanDefinition.getPropertyValues().addPropertyValue("stateLoader", new RuntimeBeanReference(resourcesStateLoaderId));
                            beanDefinition.getPropertyValues().addPropertyValue("states", statesManagedList);
                            beanDefinition.getPropertyValues().addPropertyValue("id", stateDiagramId);

                        }
                    });
                    diagramsManagedList.add(new RuntimeBeanReference(stateDiagramId));
                }
            }
        }
        LnkComponentUtils.parse(statemachineExecutorId, SmpStateMachineExecutor.class, rootElement, parserContext, new ComponentCallback() {
            public void onParse(RootBeanDefinition beanDefinition) {
                beanDefinition.getPropertyValues().addPropertyValue("stateMachine", new RuntimeBeanReference(stateMachineId));
                if (StringUtils.hasText(asyncTaskExecutorId)) {
                    beanDefinition.getPropertyValues().addPropertyValue("asyncTaskExecutor", new RuntimeBeanReference(asyncTaskExecutorId));
                }
            }
        });
        LnkComponentUtils.parse(stateMachineId, SmpStateMachine.class, rootElement, parserContext, new ComponentCallback() {
            public void onParse(RootBeanDefinition beanDefinition) {
                beanDefinition.getPropertyValues().addPropertyValue("stateMachineExecutor", new RuntimeBeanReference(statemachineExecutorId));
                beanDefinition.getPropertyValues().addPropertyValue("handlerExceptionResolver", new RuntimeBeanReference(handlerExceptionResolverId));
                beanDefinition.getPropertyValues().addPropertyValue("diagrams", diagramsManagedList);
            }
        });
        return null;
    }
}
