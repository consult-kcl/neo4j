/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.cypher.internal.compiler.v3_0.ast.convert.plannerQuery

import org.neo4j.cypher.internal.compiler.v3_0.planner.logical.plans.{SimplePatternLength, PatternRelationship, IdName}
import org.neo4j.cypher.internal.compiler.v3_0.planner._
import org.neo4j.cypher.internal.frontend.v3_0.SemanticDirection.OUTGOING
import org.neo4j.cypher.internal.frontend.v3_0.ast.{Null, SignedDecimalIntegerLiteral, PropertyKeyName}
import org.neo4j.cypher.internal.frontend.v3_0.test_helpers.CypherFunSuite

class MutatingStatementConvertersTest extends CypherFunSuite with LogicalPlanningTestSupport {

  test("setting a node property: MATCH (n) SET n.prop = 42 RETURN n") {
    val query = buildPlannerQuery("MATCH (n) SET n.prop = 42 RETURN n")
    query.horizon should equal(RegularQueryProjection(
      projections = Map("n" -> varFor("n"))
    ))

    query.queryGraph.patternNodes should equal(Set(IdName("n")))
    query.updateGraph.mutatingPatterns should equal(List(
      SetNodePropertyPattern(IdName("n"), PropertyKeyName("prop")(pos), SignedDecimalIntegerLiteral("42")(pos))
    ))
  }

  test("removing a node property should look like setting a property to null") {
    val query = buildPlannerQuery("MATCH (n) REMOVE n.prop RETURN n")
    query.horizon should equal(RegularQueryProjection(
      projections = Map("n" -> varFor("n"))
    ))

    query.queryGraph.patternNodes should equal(Set(IdName("n")))
    query.updateGraph.mutatingPatterns should equal(List(
      SetNodePropertyPattern(IdName("n"), PropertyKeyName("prop")(pos), Null()(pos))
    ))
  }

  test("setting a relationship property: MATCH (a)-[r]->(b) SET r.prop = 42 RETURN r") {
    val query = buildPlannerQuery("MATCH (a)-[r]->(b) SET r.prop = 42 RETURN r")
    query.horizon should equal(RegularQueryProjection(
      projections = Map("r" -> varFor("r"))
    ))

    query.queryGraph.patternRelationships should equal(Set(
      PatternRelationship(IdName("r"), (IdName("a"), IdName("b")), OUTGOING, List(), SimplePatternLength)
    ))
    query.updateGraph.mutatingPatterns should equal(List(
      SetRelationshipPropertyPattern(IdName("r"), PropertyKeyName("prop")(pos), SignedDecimalIntegerLiteral("42")(pos))
    ))
  }

  test("removing a relationship property should look like setting a property to null") {
    val query = buildPlannerQuery("MATCH (a)-[r]->(b) REMOVE r.prop RETURN r")
    query.horizon should equal(RegularQueryProjection(
      projections = Map("r" -> varFor("r"))
    ))

    query.queryGraph.patternRelationships should equal(Set(
      PatternRelationship(IdName("r"), (IdName("a"), IdName("b")), OUTGOING, List(), SimplePatternLength)
    ))
    query.updateGraph.mutatingPatterns should equal(List(
      SetRelationshipPropertyPattern(IdName("r"), PropertyKeyName("prop")(pos), Null()(pos))
    ))
  }

}
