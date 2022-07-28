package org.eclipse.oomph.console.configuration;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.oomph.console.core.parameters.Parameters;
import org.eclipse.oomph.setup.Product;
import org.eclipse.oomph.setup.ProductVersion;
import org.eclipse.oomph.setup.Project;
import org.eclipse.oomph.setup.SetupPackage;
import org.eclipse.oomph.setup.Stream;

@SuppressWarnings("restriction")
public class ProductVersionSelector {
    private final ResourceSet resourceSet;

    public ProductVersionSelector(ResourceSet resourceSet) {
        this.resourceSet = resourceSet;
    }

    public Product selectProduct(String productId) throws NotFoundException {
        List<Product> products = filter(SetupPackage.Literals.PRODUCT);
        Optional<Product> selectedProduct = products.stream().filter(a -> productId.equals(a.getName()))
                .findFirst();
        if (selectedProduct.isPresent()) {
            return selectedProduct.get();
        }
        throw new NotFoundException("Cannot find the product called: " + productId + System.lineSeparator()
                + "The available product(s) are: "
                + products.stream().map(Product::getName).distinct().collect(Collectors.joining(", ")));
    }

    public List<Stream> selectStreams(List<String> streamsList) throws NotFoundException {
        List<Stream> streams = filter(SetupPackage.Literals.STREAM);
        List<Stream> result = streams.stream().distinct().filter(a -> streamsList.contains(a.getName()))
                .collect(Collectors.toList());
        if (!result.isEmpty() && !streamsList.isEmpty()) {
            return result;
        }

        throw new NotFoundException("Cannot find the streams: " + streamsList.toString() + System.lineSeparator()
                + "The available stream(s) are: "
                + streams.stream().map(Stream::getName).distinct().collect(Collectors.joining(", ")));
    }

    public Project selectProject(String projectId) throws NotFoundException {
        List<Project> projects = filter(SetupPackage.Literals.PROJECT);
        Optional<Project> selectedProject = projects.stream().filter(a -> projectId.equals(a.getName()))
                .findFirst();
        if (selectedProject.isPresent()) {
            return selectedProject.get();
        }
        throw new NotFoundException("Cannot find the project called: " + projectId + System.lineSeparator()
                + "The available project(s) are: "
                + projects.stream().map(Project::getName).distinct().collect(Collectors.joining(", ")));
    }

    public List<Stream> selectProjectStreams(List<String> projectsList) throws NotFoundException {
        List<Stream> result = new LinkedList<>();
        for (String id : projectsList) {
            String[] ids = id.contains(":") ? id.split(":", 2) : new String[] {};
            String projectName = ids.length > 0 ? ids[0]
                    : System.getProperty(Parameters.OOMPH_PROJECT_ID + "." + id, id);
            String projectStream = ids.length > 0 ? ids[1]
                    : System.getProperty(Parameters.OOMPH_STREAM_ID + "." + id, Parameters.STREAM);
            Project project = selectProject(projectName);
            List<Stream> streams = project.getStreams();
            List<Stream> filtered = streams.stream().distinct()
                    .filter(a -> projectStream.equals(a.getName()))
                    .collect(Collectors.toList());
            if (filtered.isEmpty()) {
                throw new NotFoundException("Cannot find in project " + projectName + " the stream called: "
                        + projectStream + System.lineSeparator()
                        + "The available project stream(s) are: "
                        + streams.stream().map(Stream::getName).distinct().collect(Collectors.joining(", ")));
            }
            result.addAll(filtered);
        }
        return result;
    }

    public ProductVersion select(Product product, String versionId) throws NotFoundException {
        Optional<ProductVersion> version = product.getVersions().stream()
                .filter(a -> versionId.equals(a.getName())).findFirst();
        if (version.isPresent()) {
            return version.get();
        }
        throw new NotFoundException("Cannot find in product " + product.getName() + " the version called: "
                + versionId + System.lineSeparator()
                + "The available product version(s) are: "
                + product.getVersions().stream().map(ProductVersion::getName).distinct()
                        .collect(Collectors.joining(", ")));
    }

    public ProductVersion selectProductVersion(Product product, String versionId) throws NotFoundException {
        return select(product, versionId);
    }

    @SuppressWarnings("unchecked")
    private <E> List<E> filter(EClass eClass) {
        List<E> result = new LinkedList<>();
        TreeIterator<Notifier> iterator = EcoreUtil.getAllContents(resourceSet, true);
        while (iterator.hasNext()) {
            Notifier curr = iterator.next();
            if (eClass.isInstance(curr)) {
                result.add((E) curr);
            }
        }
        return result;
    }
}
