package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.fxmisc.easybind.EasyBind;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.logic.parser.ParserUtil.Option;
import seedu.address.model.person.exceptions.DuplicatePersonException;
import seedu.address.model.person.exceptions.PersonNotFoundException;
import seedu.address.model.relationship.Relationship;

/**
 * A list of persons that enforces uniqueness between its elements and does not allow nulls.
 *
 * Supports a minimal set of list operations.
 *
 * @see Person#equals(Object)
 */
public class UniquePersonList implements Iterable<Person> {

    private final ObservableList<Person> internalList = FXCollections.observableArrayList();
    // used by asObservableList()
    private final ObservableList<ReadOnlyPerson> mappedList = EasyBind.map(internalList, (person) -> person);
    private Comparator<Person> compare;
    /**
     * Returns true if the list contains an equivalent person as the given argument.
     */
    public boolean contains(ReadOnlyPerson toCheck) {
        requireNonNull(toCheck);
        return internalList.contains(toCheck);
    }

    /**
     * Adds a person to the list.
     *
     * @throws DuplicatePersonException if the person to add is a duplicate of an existing person in the list.
     */
    public void add(ReadOnlyPerson toAdd) throws DuplicatePersonException {
        requireNonNull(toAdd);
        if (contains(toAdd)) {
            throw new DuplicatePersonException();
        }
        internalList.add(new Person(toAdd));
    }

    /**
     * Replaces the person {@code target} in the list with {@code editedPerson}.
     *
     * @throws DuplicatePersonException if the replacement is equivalent to another existing person in the list.
     * @throws PersonNotFoundException if {@code target} could not be found in the list.
     */
    public void setPerson(ReadOnlyPerson target, ReadOnlyPerson editedPerson)
            throws DuplicatePersonException, PersonNotFoundException {
        requireNonNull(editedPerson);

        int index = internalList.indexOf(target);
        if (index == -1) {
            throw new PersonNotFoundException();
        }

        if (!target.equals(editedPerson) && internalList.contains(editedPerson)) {
            throw new DuplicatePersonException();
        }

        internalList.set(index, new Person(editedPerson));
    }
    //@@author TanYikai
    /**
     * Sorts the persons object according to the sortOption.
     */
    public void sort(Option sortOption) {
        requireNonNull(internalList);
        if (sortOption == Option.NAME) {
            compare = Comparator.comparing(Person::getName, Comparator.comparing(Name::toString));
        } else if (sortOption == Option.PHONE) {
            compare = Comparator.comparing(Person::getPhone, Comparator.comparing(Phone::toString));
        } else if (sortOption == Option.EMAIL) {
            compare = Comparator.comparing(Person::getEmail, Comparator.comparing(Email::toString));
        } else if (sortOption == Option.ADDRESS) {
            compare = Comparator.comparing(Person::getAddress, Comparator.comparing(Address::toString));
        } else if (sortOption == Option.REMARK) {
            compare = Comparator.comparing(Person::getRemark, Comparator.comparing(Remark::toString));
        }

        Collections.sort(internalList, compare);
    }
    //@@author
    /**
     * Removes the equivalent person from the list.
     *
     * @throws PersonNotFoundException if no such person could be found in the list.
     */
    public boolean remove(ReadOnlyPerson toRemove) throws PersonNotFoundException {
        requireNonNull(toRemove);
        Set<Relationship> relationshipsToRemove = toRemove.getRelationships();
        for (Relationship relationship: relationshipsToRemove) {
            removeRelationshipFromAddressBook(relationship);
        }
        final boolean personFoundAndDeleted = internalList.remove(toRemove);
        if (!personFoundAndDeleted) {
            throw new PersonNotFoundException();
        }
        return personFoundAndDeleted;
    }

    /**
     * To remove the existence of this relationship completely
     * As a single relationship is recorded in double-entries under the two persons involved
     */
    public boolean removeRelationshipFromAddressBook(Relationship relationshipToRemove) {
        ReadOnlyPerson fromPerson = relationshipToRemove.getFromPerson();
        ReadOnlyPerson toPerson = relationshipToRemove.getToPerson();

        ReadOnlyPerson fromPersonCopy = fromPerson.copy();
        ReadOnlyPerson toPersonCopy = toPerson.copy();

        boolean removedFromPersonRelationship = ((Person) fromPersonCopy).removeRelationship(relationshipToRemove);
        boolean removedToPersonRelationship = ((Person) toPersonCopy).removeRelationship(relationshipToRemove);
        try {
            setPerson(fromPerson, fromPersonCopy);
            setPerson(toPerson, toPersonCopy);
        } catch (DuplicatePersonException dpe) {
            assert false : "impossible as there must be relationship to be deleted";
        } catch (PersonNotFoundException psnfe) {
            assert false : "impossible as the person will definitely be there";
        }

        return removedFromPersonRelationship && removedToPersonRelationship;
    }

    public void setPersons(UniquePersonList replacement) {
        this.internalList.setAll(replacement.internalList);
    }

    public void setPersons(List<? extends ReadOnlyPerson> persons) throws DuplicatePersonException {
        final UniquePersonList replacement = new UniquePersonList();
        for (final ReadOnlyPerson person : persons) {
            replacement.add(new Person(person));
        }
        setPersons(replacement);
    }

    /**
     * Returns the backing list as an unmodifiable {@code ObservableList}.
     */
    public ObservableList<ReadOnlyPerson> asObservableList() {
        return FXCollections.unmodifiableObservableList(mappedList);
    }

    @Override
    public Iterator<Person> iterator() {
        return internalList.iterator();
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof UniquePersonList // instanceof handles nulls
                        && this.internalList.equals(((UniquePersonList) other).internalList));
    }

    @Override
    public int hashCode() {
        return internalList.hashCode();
    }
}
